package onenotepicker.demo.com.onenotepickerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;
import com.microsoft.onenote.pickerlib.OneNotePickerActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
* Created by Michael.Hancock on 10/16/2014.
*/
public class OneNoteApi {
    private static final String ENDPOINT = "https://www.onenote.com/api/v1.0/";
    private static final String ISO_8601 = "yyyy-MM-dd HH:mm:ss.SSSZ";
    private static final String TAG = "OneNote";
    private final Activity activity;
    public String liveAuthToken = "";
    public final String clientId;
    private LiveAuthClient liveClient;
    public final List<String> scopes = Arrays.asList("wl.signin", "wl.offline_access", "office.onenote_create");
    public static final int ONENOTE_PICKER_REQUEST = 100;


    public OneNoteApi(Activity a) {
        this.activity = a;
        this.clientId = a.getResources().getString(R.string.clientId);
        liveClient = new LiveAuthClient(activity, clientId);
    }

    public void signin(final Callback<String> cb) {
        liveClient.login(activity, scopes, new LiveAuthListener() {
            @Override
            public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
                if (status == LiveStatus.CONNECTED) {
                    liveAuthToken = session.getAccessToken();
                    cb.success(session.getAccessToken());
                } else {
                    Exception e = new Exception("Auth Failed " + status.name());
                    Log.e(TAG, "signin failed", e);
                    cb.failure(e);
                }
            }

            @Override
            public void onAuthError(LiveAuthException exception, Object userState) {
                cb.failure(exception);
                Log.e(TAG, "auth failed", exception);
            }
        });
    }

    public static String pageHtml(){
        String date = new SimpleDateFormat(ISO_8601).format(new Date());
        final String html = "<html>" +
                "<head>" +
                "<title>A Sample Android App Page</title>" +
                "<meta name=\"created\" content=\"" + date + "\" />" +
                "</head>" +
                "<body>" +
                "<p>This is a page that just contains some simple <i>formatted</i> <b>text</b></p>" +
                "<p>Here is a <a href=\"http://www.microsoft.com\">link</a></p>" +
                "<p>Here is an image: <img src=\"http://i.microsoft.com/global/en-us/news/publishingimages/homepage/highlights/prod_xboxone_hl.jpg\" />" +
                "</body>" +
                "</html>";
        return html;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createPage(final String html, final String section, final Callback<String> cb ){


        new AsyncTask(){

            @Override
            protected Object doInBackground(Object... params) {


                try {

                    HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT+"sections/"+section+"/pages").openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Content-Type", "text/html");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer "+liveAuthToken);

                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.write(html.getBytes("UTF-8"));
                    out.flush();
                    out.close();

                    cb.success(streamToString( conn.getInputStream() ));


                } catch (Exception e) {
                    cb.failure(e);
                }



                return null;
            }
        }.execute();




    }

    public static String streamToString(InputStream is) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


//
//    public void showPicker() {
//        Intent oneNotePickerIntent = new Intent(activity, OneNotePickerActivity.class);
//        oneNotePickerIntent.putExtra("ACCESS_TOKEN", liveAuthToken);
//        activity.startActivityForResult(oneNotePickerIntent, ONENOTE_PICKER_REQUEST);
//    }
//
//
//
//    public void handlePickerResult(int resultCode, Intent data, Callback<OneNoteSection> uiCallback) {
//
//        if (resultCode == Activity.RESULT_OK) {
//            OneNoteSection s = new OneNoteSection();
//            s.sectionID = data.getExtras().getString("SECTION_ID");
//            s.sectionName = data.getExtras().getString("SECTION_NAME");
//            s.pagesURL = (URL) data.getExtras().get("PAGES_URL");
//            s.createdTime = (Date) data.getExtras().get("CREATED_TIME");
//            s.modifiedTime = (Date) data.getExtras().get("MODIFIED_TIME");
//            s.lastModifiedBy = data.getExtras().getString("LAST_MODIFIED_BY");
//
//            //DO SOMETHING WITH THE INFO
//            uiCallback.success(s);
//        }
//
//        //PICKER CANCELLED OR generated an error
//        else if (resultCode == Activity.RESULT_CANCELED) {
//            if (data.getExtras().getBoolean("USER_CANCELLED")) {
//
//                //USER CANCELLED OPERATION.
//                uiCallback.failure(new UserCancelledException());
//
//            } else if (data.getExtras().getBoolean("API_ERROR")) {
//                //API BASED ERROR. LOAD ERROR INFO
//                String apiErrorCode = data.getExtras().getString("API_ERROR_CODE");
//                String apiErrorString = data.getExtras().getString("API_ERROR_STRING");
//                URL apiErrorURL = (URL) data.getExtras().get("API_ERROR_URL");
//
//                //DO SOMETHING WITH ERROR INFO
//                uiCallback.failure(new Exception(apiErrorCode + " ["+apiErrorCode+"]"));
//
//            } else {
//                //SYSTEM EXCEPTION. LOAD EXCEPTION
//                Exception e = (Exception) data.getExtras().get("SYSTEM_EXCEPTION");
//
//                //HANDLE EXCEPTION
//                uiCallback.failure(e);
//
//            }
//        }
//    }
}
