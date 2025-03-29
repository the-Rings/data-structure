package rain.mocking.design;

public class Main {
    private static String sayHello() {
        return "Hello world!";
    }
    public static void main(String[] args) {
        String words = sayHello();
        System.out.println(words);
        System.out.println("Hello world!");
        
    }
}