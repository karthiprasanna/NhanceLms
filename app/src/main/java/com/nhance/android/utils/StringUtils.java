package com.nhance.android.utils;

public class StringUtils {


//    #NEWS-FEEDS
     public static final String TXT_YOUR = "Your";
    public static final String TXT_A_MEMBER = "A Member";
    public static final String ATTEMPTED_BY = "attempted by";
    public static final String  ADDED_BY = "added by";
    public static final String SHARED_BY = "shared by";
    public static final String COMMENTED_BY = "commented by";
    public static final String FOLLOWED_BY = "followed by";
    public static final String SOLUTION_ADDED_BY = "solution added by";
    public static final String  NEWS_FEED_UPVOTED = "upvoted";
    public static final String  NEWS_FEED_ADDED_BY = "added by";
    public static final String NEWS_FEED_ON = "on";
    public static final String NEWS_FEED_FOLLOWING = "following";
    public static final String NEWS_FEED_FOLLOWED = "followed";
    public static final String NEWS_FEED_ADDED = "added";
    public static final String NEWS_FEED_NEW = "new";
    public static final String NEWS_FEED_ADDED_A_NEW = "added a new";
    public static final String  NEWS_FEED_NEW_SOLUTION_TO = "new solution to";
    public static final String NEWS_FEED_ADDED_TO_YOUR_LIB = "to your library";
    public static final String NEWS_FEED_CREATED_BY = "created by";
    public static final String NEWS_FEED_SHARED = "shared";
    public static final String NEWS_FEED_ATTEMPTED = "attempted";
    public static final String NEWS_FEED_COMMENTED_ON = "commented on";
    public static final String NEWS_FEED_COMMENTED = "commented";
    public static final String NEWS_FEED_ASKED = "asked";
    public static final String NEWS_FEED_ANSWERED = "answered";
    public static final String NEWS_FEED_RESULT_OF = "The result of the";
    public static final String NEWS_FEED_RESULTS_OUT_CHECK_OUT = "is out. Check it out!";
    public static final String NEWS_FEED_PROVIDED_SOULTION = "provided a solution to";
    public static final String NEWS_FEED_NEW_STATUS_FEED = "new status feed";
    public static final String NEWS_FEED_IMAGE_IN_FEED = "Image in status feed";
    public static final String NEWS_FEED_VIDEO_IN_FEED = "Video in status feed";
    public static final String NEWS_FEED_WEB_PAGE_IN_FEED = "Web Page Link in status feed";
    public static final String TXT_SOLUTION = "solution";
    public static final String TXT_ANSWER = "answer";
    public static final String TXT_ON = "on";
    public static final String TXT_COMMENT = "comment";
    public static final String TXT_YOU = "You";

//#ENTITY
    public static final String ENTITY_ASSIGNMENT = "Assignment";
    public static final String ENTITY_TEST = "Test";
    public static final String ENTITY_DOCUMENT = "Document";
    public static final String ENTITY_VIDEO = "Video";
    public static final String ENTITY_FILE = "File";
    public static final String ENTITY_MODULE = "Module";
    public static final String ENTITY_QUES = "Question";
    public static final String ENTITY_REMARK = "Remark";
    public static final String ENTITY_CHALLENGE = "Challenge";
    public static final String ENTITY_DOUBT = "Doubt";
    public static final String ENTITY_FEED = "Feed";

//    #PROFILE
    public static final String PROFILE_TEACHER = "Teacher";
    public static final String PROFILE_STUDENT = "Student";
    public static final String PROFILE_MANAGER = "Manager";
    public static final String  PROFILE_EDITOR = "Manager";

    public static final String ROLE_STUDENT = "Student";
    public static final String ROLE_TEACHER = "Teacher";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_PARENT = "Parent";
    public static final String ROLE_EDITOR = "Editor";
    public static final String POST_REMARK_FOR_USER = "Post a remark for your student";
    public static final String PROFILE_DATA_NOT_PROVIDED = "Not Provided";
//    public static final String member_MEMBER_INFO = %s's Organization Member info
//    public static final String QUESTION_ATTEMPTED_INFO = The count shows questions attempted in all the tests.



    public final static boolean isValidEmail(CharSequence target) {

        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static byte[] parseHexBinary(String s) {

        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0)
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i + 1));
            if (h == -1 || l == -1)
                throw new IllegalArgumentException("contains illegal character[h:" + s.charAt(i)
                        + ", l:" + s.charAt(i + 1) + "] for hexBinary: " + s);

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static int hexToBin(char ch) {

        if ('0' <= ch && ch <= '9')
            return ch - '0';
        if ('A' <= ch && ch <= 'F')
            return ch - 'A' + 10;
        if ('a' <= ch && ch <= 'f')
            return ch - 'a' + 10;
        return -1;
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {

        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public static String htmlEncode(String s) {

        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;"); //$NON-NLS-1$
                break;
            case '\'':
                // http://www.w3.org/TR/xhtml1
                // The named character reference &apos; (the apostrophe, U+0027) was introduced in
                // XML 1.0 but does not appear in HTML. Authors should therefore use &#39; instead
                // of &apos; to work as expected in HTML 4 user agents.
                sb.append("&#39;"); //$NON-NLS-1$
                break;
            case '"':
                sb.append("&quot;"); //$NON-NLS-1$
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
