package ai.medialab.rndfpana;

public class SessionInfo {
    private static String userId;
    private static String baseUrl;
    private static int age;
    private static String gender;

    public static void set(String userId, String baseUrl, int age, String gender) {
        SessionInfo.userId = userId;
        SessionInfo.baseUrl = baseUrl;
        SessionInfo.age = age;
        SessionInfo.gender = gender;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static int getAge() {
        return age;
    }

    public static String getGender() {
        return gender;
    }
}
