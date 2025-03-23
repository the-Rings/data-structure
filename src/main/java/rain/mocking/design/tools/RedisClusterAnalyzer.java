import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;
import java.text.SimpleDateFormat;

/**
 * Redis集群数据分析工具 - 用于识别数据倾斜问题并计算各个key及其对应节点的内存占用
 */
public class RedisClusterAnalyzer {

    private final JedisCluster jedisCluster;
    private final int scanBatchSize;
    private final ExecutorService executorService;
    
    // 保存每个节点的key数量统计
    private final Map<String, Long> nodeKeyCountMap = new ConcurrentHashMap<>();
    // 保存每个节点的内存占用统计
    private final Map<String, AtomicLong> nodeMemoryUsageMap = new ConcurrentHashMap<>();
    // 保存最大的keys，用于识别可能导致数据倾斜的大key
    private final PriorityQueue<KeyMemoryInfo> topKeys;
    
    private final int topKeysCount;
    private final String reportKeyPrefix;
    
    /**
     * 构造函数
     * @param nodes Redis集群节点列表
     * @param password Redis密码，如果没有则传null
     * @param connectionTimeout 连接超时时间
     * @param soTimeout Socket超时时间
     * @param scanBatchSize SCAN命令每次扫描的key数量
     * @param maxThreads 最大线程数
     * @param topKeysCount 保留top N大小的key
     * @param reportKeyPrefix 保存报告的Redis key前缀
     */
    public RedisClusterAnalyzer(Set<HostAndPort> nodes, String password, 
                               int connectionTimeout, int soTimeout, 
                               int scanBatchSize, int maxThreads, int topKeysCount,
                               String reportKeyPrefix) {
        this.jedisCluster = new JedisCluster(nodes, connectionTimeout, soTimeout, 
                                            5, password, null);
        this.scanBatchSize = scanBatchSize;
        this.executorService = Executors.newFixedThreadPool(1); // 使用单线程以减少资源占用
        this.topKeysCount = topKeysCount;
        this.topKeys = new PriorityQueue<>(topKeysCount, Comparator.comparingLong(KeyMemoryInfo::getMemoryBytes));
        this.reportKeyPrefix = reportKeyPrefix;
    }
    
    /**
     * 分析Redis集群数据分布
     * @return 分析结果
     */
    public AnalysisResult analyzeCluster() throws InterruptedException, ExecutionException {
        System.out.println("开始分析Redis集群数据分布...");
        long startTime = System.currentTimeMillis();
        
        // 获取所有集群节点
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        System.out.println("集群节点数: " + clusterNodes.size());
        
        // 逐个分析节点，而不是并行分析所有节点
        List<NodeAnalysisResult> nodeResults = new ArrayList<>();
        for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
            String nodeId = entry.getKey();
            JedisPool pool = entry.getValue();
            nodeKeyCountMap.put(nodeId, 0L);
            nodeMemoryUsageMap.put(nodeId, new AtomicLong(0));
            
            // 直接调用分析方法，不使用线程池
            NodeAnalysisResult result = analyzeNode(nodeId, pool);
            nodeResults.add(result);
            
            // 每分析完一个节点休息一段时间，减轻Redis负载
            TimeUnit.SECONDS.sleep(2);
        }
        
        // 关闭线程池
        executorService.shutdown();
        
        // 计算总体统计信息
        long totalKeys = nodeKeyCountMap.values().stream().mapToLong(Long::longValue).sum();
        long totalMemoryBytes = nodeMemoryUsageMap.values().stream().mapToLong(AtomicLong::get).sum();
        
        // 获取数据倾斜情况
        Map<String, Double> nodeMemoryPercentage = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : nodeMemoryUsageMap.entrySet()) {
            double percentage = totalMemoryBytes > 0 ? 
                                (double) entry.getValue().get() / totalMemoryBytes * 100 : 0;
            nodeMemoryPercentage.put(entry.getKey(), percentage);
        }
        
        // 获取最大的keys
        List<KeyMemoryInfo> largestKeys = new ArrayList<>(topKeysCount);
        KeyMemoryInfo keyInfo;
        while ((keyInfo = topKeys.poll()) != null) {
            largestKeys.add(0, keyInfo); // 插入到头部，形成降序排列
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("分析完成，耗时: " + duration + "ms");
        
        return new AnalysisResult(
            totalKeys,
            totalMemoryBytes,
            nodeKeyCountMap,
            nodeMemoryPercentage,
            largestKeys,
            nodeResults,
            duration
        );
    }
    
    /**
     * 分析单个节点的数据
     */
    private NodeAnalysisResult analyzeNode(String nodeId, JedisPool pool) {
        long startTime = System.currentTimeMillis();
        long keyCount = 0;
        long memoryUsage = 0;
        
        try (var jedis = pool.getResource()) {
            System.out.println("开始分析节点: " + nodeId);
            
            // 检查是否是主节点
            String info = jedis.info("Replication");
            if (info.contains("role:slave")) {
                System.out.println("跳过从节点: " + nodeId);
                return new NodeAnalysisResult(nodeId, 0, 0, Collections.emptyList(), 0);
            }
            
            // 使用SCAN命令遍历所有key
            String cursor = "0";
            ScanParams scanParams = new ScanParams().count(scanBatchSize);
            List<KeyMemoryInfo> nodeLargestKeys = new ArrayList<>();
            
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = scanResult.getCursor();
                List<String> keys = scanResult.getResult();
                
                if (keys.isEmpty()) continue;
                
                // 批量处理keys
                for (String key : keys) {
                    TimeUnit.MILLISECONDS.sleep(500); // 增加间隔时间减轻Redis负担
                    try {
                        // 直接处理key，不使用额外线程，避免线程切换开销
                        String keyType = jedis.type(key);
                        long keySize = getValueSize(jedis, key, keyType);
                        
                        // 更新节点统计
                        keyCount++;
                        memoryUsage += keySize;
                        
                        // 更新全局统计
                        nodeKeyCountMap.put(nodeId, nodeKeyCountMap.get(nodeId) + 1);
                        nodeMemoryUsageMap.get(nodeId).addAndGet(keySize);
                        
                        // 更新top keys
                        updateTopKeys(new KeyMemoryInfo(key, nodeId, keySize, keyType));
                        
                        // 定期打印进度
                        if (keyCount % 10000 == 0) {
                            System.out.printf("节点%s已处理%d个keys, 内存使用:%dMB%n", 
                                             nodeId, keyCount, memoryUsage / (1024 * 1024));
                        }
                    } catch (Exception e) {
                        System.err.println("处理key失败: " + key + ", 错误: " + e.getMessage());
                    }
                }
                
            } while (!cursor.equals("0"));
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("节点%s分析完成，共%d个keys，总内存:%dMB，耗时:%dms%n", 
                             nodeId, keyCount, memoryUsage / (1024 * 1024), duration);
            
            return new NodeAnalysisResult(nodeId, keyCount, memoryUsage, nodeLargestKeys, duration);
        } catch (Exception e) {
            System.err.println("分析节点失败: " + nodeId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return new NodeAnalysisResult(nodeId, 0, 0, Collections.emptyList(), 0);
        }
    }
    
    /**
     * 根据key类型获取value大小
     */
    private long getValueSize(redis.clients.jedis.Jedis jedis, String key, String type) {
        try {
            switch (type) {
                case "string":
                    return jedis.strlen(key);
                    
                case "hash":
                    long hashSize = 0;
                    long hashLength = jedis.hlen(key);
                    
                    // 对于大型hash，采用更谨慎的采样方式估算
                    if (hashLength > 1000) {
                        int hashSampleSize = Math.min(50, (int)(hashLength * 0.01)); // 采样更少的元素
                        Map<String, String> hashSample = new HashMap<>();
                        
                        String scanCursor = "0";
                        ScanParams hashScanParams = new ScanParams().count(10); // 减少一次性获取的数量
                        int sampleCount = 0;
                        
                        // 最多循环10次来获取样本，避免长时间占用连接
                        int loopCount = 0;
                        do {
                            ScanResult<Map.Entry<String, String>> hashScanResult = 
                                jedis.hscan(key, scanCursor, hashScanParams);
                            scanCursor = hashScanResult.getCursor();
                            
                            for (Map.Entry<String, String> entry : hashScanResult.getResult()) {
                                if (sampleCount < hashSampleSize) {
                                    hashSample.put(entry.getKey(), entry.getValue());
                                    sampleCount++;
                                } else {
                                    break;
                                }
                            }
                            
                            loopCount++;
                            // 如果已经采样足够或者扫描完毕或者达到最大循环次数，则退出循环
                            if (sampleCount >= hashSampleSize || scanCursor.equals("0") || loopCount >= 10) {
                                break;
                            }
                            
                            // 每次循环之间增加短暂延迟
                            TimeUnit.MILLISECONDS.sleep(100);
                        } while (true);
                        
                        // 计算样本大小
                        long hashSampleBytes = 0;
                        for (Map.Entry<String, String> e : hashSample.entrySet()) {
                            hashSampleBytes += e.getKey().getBytes().length + 
                                    (e.getValue() != null ? e.getValue().getBytes().length : 0);
                        }
                        
                        // 估算总大小
                        if (sampleCount > 0) {
                            double avgEntrySize = hashSampleBytes / (double) sampleCount;
                            hashSize = (long) (avgEntrySize * hashLength);
                        }
                    } else {
                        // 小型hash可以直接获取全部
                        Map<String, String> hash = jedis.hgetAll(key);
                        for (Map.Entry<String, String> e : hash.entrySet()) {
                            hashSize += e.getKey().getBytes().length + 
                                    (e.getValue() != null ? e.getValue().getBytes().length : 0);
                        }
                    }
                    return hashSize;
                    
                case "list":
                    long listSize = 0;
                    long listLength = jedis.llen(key);
                    
                    // 对于大列表，减少采样数量
                    int listSampleSize = Math.min(50, (int)Math.ceil(listLength * 0.01));
                    if (listSampleSize > 0) {
                        List<String> samples = jedis.lrange(key, 0, listSampleSize - 1);
                        long sampleBytes = samples.stream()
                                .mapToLong(s -> s != null ? s.getBytes().length : 0)
                                .sum();
                        
                        if (samples.size() > 0) {
                            double avgSize = sampleBytes / (double) samples.size();
                            listSize = (long) (avgSize * listLength);
                        }
                    }
                    return listSize;
                    
                case "set":
                    long setSize = 0;
                    long setCount = jedis.scard(key);
                    
                    // 对于大型set，减少采样数量
                    if (setCount > 1000) {
                        int setSampleSize = Math.min(50, (int)(setCount * 0.01));
                        List<String> setSamples = new ArrayList<>();
                        
                        String setCursor = "0";
                        ScanParams setScanParams = new ScanParams().count(10); // 减少一次性获取的数量
                        int sampleCount = 0;
                        int loopCount = 0;
                        
                        // 最多循环10次
                        do {
                            ScanResult<String> setScanResult = jedis.sscan(key, setCursor, setScanParams);
                            setCursor = setScanResult.getCursor();
                            
                            for (String member : setScanResult.getResult()) {
                                if (sampleCount < setSampleSize) {
                                    setSamples.add(member);
                                    sampleCount++;
                                } else {
                                    break;
                                }
                            }
                            
                            loopCount++;
                            if (sampleCount >= setSampleSize || setCursor.equals("0") || loopCount >= 10) {
                                break;
                            }
                            
                            TimeUnit.MILLISECONDS.sleep(100);
                        } while (true);
                        
                        long setSampleBytes = setSamples.stream()
                                .mapToLong(s -> s != null ? s.getBytes().length : 0)
                                .sum();
                        
                        if (sampleCount > 0) {
                            double avgSize = setSampleBytes / (double) sampleCount;
                            setSize = (long) (avgSize * setCount);
                        }
                    } else {
                        // 小型set可以直接获取全部
                        Set<String> members = jedis.smembers(key);
                        for (String member : members) {
                            setSize += member != null ? member.getBytes().length : 0;
                        }
                    }
                    return setSize;
                    
                case "zset":
                    long zsetSize = 0;
                    long zsetCount = jedis.zcard(key);
                    
                    // 对于大型zset，减少采样数量
                    if (zsetCount > 1000) {
                        int zsetSampleSize = Math.min(50, (int)(zsetCount * 0.01));
                        
                        // 使用zscan代替zrange，避免一次性获取过多数据
                        Set<String> zsetSamples = new HashSet<>();
                        String zsetCursor = "0";
                        ScanParams zsetScanParams = new ScanParams().count(10);
                        int sampleCount = 0;
                        int loopCount = 0;
                        
                        do {
                            ScanResult<redis.clients.jedis.Tuple> zsetScanResult = 
                                jedis.zscan(key, zsetCursor, zsetScanParams);
                            zsetCursor = zsetScanResult.getCursor();
                            
                            for (redis.clients.jedis.Tuple tuple : zsetScanResult.getResult()) {
                                if (sampleCount < zsetSampleSize) {
                                    zsetSamples.add(tuple.getElement());
                                    sampleCount++;
                                } else {
                                    break;
                                }
                            }
                            
                            loopCount++;
                            if (sampleCount >= zsetSampleSize || zsetCursor.equals("0") || loopCount >= 10) {
                                break;
                            }
                            
                            TimeUnit.MILLISECONDS.sleep(100);
                        } while (true);
                        
                        long zsetSampleBytes = zsetSamples.stream()
                                .mapToLong(s -> s != null ? s.getBytes().length : 0)
                                .sum();
                        
                        if (sampleCount > 0) {
                            double avgSize = zsetSampleBytes / (double) sampleCount;
                            // 每个元素额外加上分数的大小(8字节)
                            zsetSize = (long) ((avgSize + 8) * zsetCount);
                        }
                    } else {
                        // 小型zset可以直接获取全部
                        Set<String> zsetMembers = jedis.zrange(key, 0, -1);
                        for (String member : zsetMembers) {
                            zsetSize += member != null ? member.getBytes().length : 0;
                            // 分数也占空间
                            zsetSize += 8; // double是8字节
                        }
                    }
                    return zsetSize;
                    
                default:
                    return 0;
            }
        } catch (Exception e) {
            System.err.println("计算key [" + key + "] 大小出错: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 更新top keys队列
     */
    private synchronized void updateTopKeys(KeyMemoryInfo keyInfo) {
        topKeys.offer(keyInfo);
        // 保持队列大小不超过topKeysCount
        if (topKeys.size() > topKeysCount) {
            topKeys.poll(); // 移除最小的元素
        }
    }
    
    /**
     * 生成数据倾斜报告并保存到Redis
     */
    public void saveAnalysisReportToRedis(AnalysisResult result) {
        // 生成报告内容
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("===== Redis集群数据分析报告 =====\n");
        reportBuilder.append("分析时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        reportBuilder.append("分析耗时: ").append(result.getProcessingTime()).append("ms\n");
        reportBuilder.append("总key数量: ").append(result.getTotalKeys()).append("\n");
        reportBuilder.append("总内存使用: ").append(formatSize(result.getTotalMemoryBytes())).append("\n\n");
        
        reportBuilder.append("----- 节点数据分布 -----\n");
        List<Map.Entry<String, Double>> sortedNodes = 
            result.getNodeMemoryPercentage().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        // 防止除零错误
        long totalKeys = Math.max(1, result.getTotalKeys());
        
        for (Map.Entry<String, Double> entry : sortedNodes) {
            String nodeId = entry.getKey();
            long keyCount = result.getNodeKeyCountMap().getOrDefault(nodeId, 0L);
            double memPercentage = entry.getValue();
            long memBytes = (long)(result.getTotalMemoryBytes() * memPercentage / 100);
            
            reportBuilder.append(String.format("节点: %s\n  Key数量: %d (%.2f%%)\n  内存使用: %s (%.2f%%)\n",
                           nodeId,
                           keyCount,
                           (double)keyCount / totalKeys * 100,
                           formatSize(memBytes),
                           memPercentage));
        }
        
        reportBuilder.append("\n----- 最大Key列表 -----\n");
        for (int i = 0; i < Math.min(result.getLargestKeys().size(), 20); i++) {
            KeyMemoryInfo key = result.getLargestKeys().get(i);
            reportBuilder.append(String.format("%d. Key: %s\n   类型: %s\n   大小: %s\n   节点: %s\n",
                           i + 1,
                           key.getKey(),
                           key.getType(),
                           formatSize(key.getMemoryBytes()),
                           key.getNodeId()));
        }
        
        reportBuilder.append("\n===== 数据倾斜分析 =====\n");
        double maxPercentage = sortedNodes.isEmpty() ? 0 : sortedNodes.get(0).getValue();
        double minPercentage = sortedNodes.isEmpty() ? 0 : 
                             sortedNodes.get(sortedNodes.size() - 1).getValue();
        
        reportBuilder.append(String.format("内存使用最高的节点: %s (%.2f%%)\n", 
                       sortedNodes.isEmpty() ? "无" : sortedNodes.get(0).getKey(), 
                       maxPercentage));
        reportBuilder.append(String.format("内存使用最低的节点: %s (%.2f%%)\n",
                       sortedNodes.isEmpty() ? "无" : sortedNodes.get(sortedNodes.size() - 1).getKey(),
                       minPercentage));
        reportBuilder.append(String.format("最大/最小内存比率: %.2f\n", 
                       minPercentage > 0 ? maxPercentage / minPercentage : 0));
        
        // 数据倾斜判断标准：最大节点占比超过平均值的1.5倍
        double avgPercentage = 100.0 / sortedNodes.size();
        boolean isSkewed = maxPercentage > avgPercentage * 1.5;
        
        reportBuilder.append("\n数据分布状态: ").append(isSkewed ? "存在明显数据倾斜" : "数据分布相对均衡").append("\n");
        
        if (isSkewed) {
            reportBuilder.append("\n----- 数据倾斜解决建议 -----\n");
            reportBuilder.append("1. 重新设计热点key的数据结构，考虑拆分\n");
            reportBuilder.append("2. 对大key进行拆分，例如将大hash拆分为多个小hash\n");
            reportBuilder.append("3. 检查是否存在不合理的数据设计或过期策略\n");
            reportBuilder.append("4. 考虑调整集群槽位分配\n");
            
            if (!result.getLargestKeys().isEmpty()) {
                reportBuilder.append("\n建议优先处理的大key:\n");
                for (int i = 0; i < Math.min(result.getLargestKeys().size(), 5); i++) {
                    KeyMemoryInfo key = result.getLargestKeys().get(i);
                    reportBuilder.append(String.format("%d. %s (类型:%s, 大小:%s)\n",
                                   i + 1,
                                   key.getKey(),
                                   key.getType(),
                                   formatSize(key.getMemoryBytes())));
                }
            }
        }
        
        // 单独保存Top 100大key
        StringBuilder topKeysBuilder = new StringBuilder();
        topKeysBuilder.append("===== Top 100 大Key列表 =====\n");
        topKeysBuilder.append("分析时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        for (int i = 0; i < result.getLargestKeys().size(); i++) {
            KeyMemoryInfo key = result.getLargestKeys().get(i);
            topKeysBuilder.append(String.format("%d. Key: %s\n   类型: %s\n   大小: %s\n   节点: %s\n\n",
                           i + 1,
                           key.getKey(),
                           key.getType(),
                           formatSize(key.getMemoryBytes()),
                           key.getNodeId()));
        }
        
        // 生成报告key名称，包含时间戳
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String summaryReportKey = reportKeyPrefix + ":summary:" + timestamp;
        String topKeysReportKey = reportKeyPrefix + ":topkeys:" + timestamp;
        
        // 保存报告到Redis
        try {
            jedisCluster.set(summaryReportKey, reportBuilder.toString());
            System.out.println("分析摘要报告已保存到Redis key: " + summaryReportKey);
            
            jedisCluster.set(topKeysReportKey, topKeysBuilder.toString());
            System.out.println("大Key详细报告已保存到Redis key: " + topKeysReportKey);
            
            // 设置过期时间，例如7天
            jedisCluster.expire(summaryReportKey, 7 * 24 * 60 * 60);
            jedisCluster.expire(topKeysReportKey, 7 * 24 * 60 * 60);
            
            // 保存索引key，用于查询最近的报告
            String indexKey = reportKeyPrefix + ":reports";
            jedisCluster.lpush(indexKey, summaryReportKey, topKeysReportKey);
            jedisCluster.ltrim(indexKey, 0, 9); // 只保留最近10份报告
            jedisCluster.expire(indexKey, 30 * 24 * 60 * 60); // 30天过期
            
            // 打印报告内容到控制台方便查看
            System.out.println("\n" + reportBuilder.toString());
        } catch (Exception e) {
            System.err.println("保存报告到Redis失败: " + e.getMessage());
            e.printStackTrace();
            
            // 保存失败时，打印报告到控制台
            System.out.println("\n===== 由于保存到Redis失败，报告内容仅打印在控制台 =====");
            System.out.println(reportBuilder.toString());
        }
    }
    
    /**
     * 处理异常并将堆栈信息保存到Redis
     */
    public void saveExceptionToRedis(Exception e, String operationDescription) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String errorKey = reportKeyPrefix + ":error:" + timestamp;
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            String errorReport = "===== Redis分析工具错误报告 =====\n" +
                               "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n" +
                               "操作: " + operationDescription + "\n" +
                               "错误信息: " + e.getMessage() + "\n\n" +
                               "堆栈跟踪:\n" + sw.toString();
            
            jedisCluster.set(errorKey, errorReport);
            jedisCluster.expire(errorKey, 7 * 24 * 60 * 60); // 7天过期
            
            System.err.println("错误信息已保存到Redis key: " + errorKey);
        } catch (Exception ex) {
            System.err.println("保存错误信息到Redis失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 格式化内存大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 单个Key的内存占用信息
     */
    public static class KeyMemoryInfo {
        private final String key;
        private final String nodeId;
        private final long memoryBytes;
        private final String type;
        
        public KeyMemoryInfo(String key, String nodeId, long memoryBytes, String type) {
            this.key = key;
            this.nodeId = nodeId;
            this.memoryBytes = memoryBytes;
            this.type = type;
        }
        
        public String getKey() { return key; }
        public String getNodeId() { return nodeId; }
        public long getMemoryBytes() { return memoryBytes; }
        public String getType() { return type; }
    }
    
    /**
     * 单个节点的分析结果
     */
    public static class NodeAnalysisResult {
        private final String nodeId;
        private final long keyCount;
        private final long memoryUsage;
        private final List<KeyMemoryInfo> largestKeys;
        private final long processingTime;
        
        public NodeAnalysisResult(String nodeId, long keyCount, long memoryUsage, 
                                 List<KeyMemoryInfo> largestKeys, long processingTime) {
            this.nodeId = nodeId;
            this.keyCount = keyCount;
            this.memoryUsage = memoryUsage;
            this.largestKeys = largestKeys;
            this.processingTime = processingTime;
        }
    }
    
    /**
     * 整体分析结果
     */
    public static class AnalysisResult {
        private final long totalKeys;
        private final long totalMemoryBytes;
        private final Map<String, Long> nodeKeyCountMap;
        private final Map<String, Double> nodeMemoryPercentage;
        private final List<KeyMemoryInfo> largestKeys;
        private final List<NodeAnalysisResult> nodeResults;
        private final long processingTime;
        
        public AnalysisResult(long totalKeys, long totalMemoryBytes, 
                             Map<String, Long> nodeKeyCountMap,
                             Map<String, Double> nodeMemoryPercentage,
                             List<KeyMemoryInfo> largestKeys,
                             List<NodeAnalysisResult> nodeResults,
                             long processingTime) {
            this.totalKeys = totalKeys;
            this.totalMemoryBytes = totalMemoryBytes;
            this.nodeKeyCountMap = nodeKeyCountMap;
            this.nodeMemoryPercentage = nodeMemoryPercentage;
            this.largestKeys = largestKeys;
            this.nodeResults = nodeResults;
            this.processingTime = processingTime;
        }
        
        public long getTotalKeys() { return totalKeys; }
        public long getTotalMemoryBytes() { return totalMemoryBytes; }
        public Map<String, Long> getNodeKeyCountMap() { return nodeKeyCountMap; }
        public Map<String, Double> getNodeMemoryPercentage() { return nodeMemoryPercentage; }
        public List<KeyMemoryInfo> getLargestKeys() { return largestKeys; }
        public List<NodeAnalysisResult> getNodeResults() { return nodeResults; }
        public long getProcessingTime() { return processingTime; }
    }
    
    /**
     * 主方法示例
     */
    public static void main(String[] args) {
        // 创建Redis集群连接
        Set<HostAndPort> nodes = new HashSet<>();
        // 添加集群节点，根据实际环境替换IP和端口
        nodes.add(new HostAndPort("redis-node1", 6379));
        nodes.add(new HostAndPort("redis-node2", 6379));
        nodes.add(new HostAndPort("redis-node3", 6379));
        
        // 用于存储报告的Redis key前缀
        String reportKeyPrefix = "redis:analyzer:report";
        
        // 创建分析器实例
        RedisClusterAnalyzer analyzer = new RedisClusterAnalyzer(
            nodes,                  // 集群节点
            "password",             // 密码，如果没有则传null
            5000,                   // 连接超时(ms)
            10000,                  // Socket超时(ms)
            100,                    // 每次扫描100个key
            Runtime.getRuntime().availableProcessors(), // 线程数等于CPU核心数
            100,                    // 保留100个最大key
            reportKeyPrefix         // 报告key前缀
        );
        
        try {
            // 执行分析
            AnalysisResult result = analyzer.analyzeCluster();
            
            // 保存分析报告到Redis
            analyzer.saveAnalysisReportToRedis(result);
            
        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            
            // 保存异常信息到Redis
            analyzer.saveExceptionToRedis(e, "analyzeCluster");
        } finally {
            // 关闭资源
            analyzer.close();
        }
    }
}
