package onenotepicker.demo.com.onenotepickerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.microsoft.onenote.pickerlib.OneNotePickerActivity;

import java.net.URL;
import java.util.Date;


public class MyActivity extends Activity {

    private View btnCreateDoc;
    private View btnSignin;
    private OneNoteApi oneNoteApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        btnSignin = findViewById(R.id.signin);
        btnCreateDoc = findViewById(R.id.createDoc);

        this.oneNoteApi = new OneNoteApi(this);
    }

    public void signin(View v) {
        oneNoteApi.signin(new UICallback<String>(this) {
            @Override
            public void onSuccess(String token) {
                toast("Auth success");
                btnSignin.setEnabled(false);
                btnCreateDoc.setEnabled(true);
            }

            @Override
            public void onFailure(Throwable error) {
                toast("Auth Failed");
            }

        });
    }

    public static final int ONENOTE_PICKER_REQUEST = 100;
    public void pick(View v) {
        Intent oneNotePickerIntent = new Intent(this, OneNotePickerActivity.class);
        oneNotePickerIntent.putExtra("ACCESS_TOKEN", oneNoteApi.liveAuthToken);
        startActivityForResult(oneNotePickerIntent, ONENOTE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == OneNoteApi.ONENOTE_PICKER_REQUEST) {

            handlePickerResult(resultCode, data, new UICallback<OneNoteSection>(this) {

                @Override
                public void onSuccess(OneNoteSection result) {
                    toast("Creating Document in Section " + result.sectionName);

                    oneNoteApi.createPage(OneNoteApi.pageHtml(), result.sectionID, new UICallback<String>(MyActivity.this) {
                        @Override
                        public void onSuccess(String result) {
                            toast("Success");
                        }

                        @Override
                        public void onFailure(Throwable result) {
                            toast("failed to create document");
                        }
                    });
                }

                @Override
                public void onFailure(Throwable result) {
                    if (result instanceof UserCancelledException) {
                        toast("Cancelled");
                    } else
                        toast("Picker Failed " + result.getMessage());
                }
            });
        }
    }

    public void handlePickerResult(int resultCode, Intent data, Callback<OneNoteSection> uiCallback) {

        if (resultCode == Activity.RESULT_OK) {
            OneNoteSection s = new OneNoteSection();
            s.sectionID = data.getExtras().getString("SECTION_ID");
            s.sectionName = data.getExtras().getString("SECTION_NAME");
            s.pagesURL = (URL) data.getExtras().get("PAGES_URL");
            s.createdTime = (Date) data.getExtras().get("CREATED_TIME");
            s.modifiedTime = (Date) data.getExtras().get("MODIFIED_TIME");
            s.lastModifiedBy = data.getExtras().getString("LAST_MODIFIED_BY");

            //DO SOMETHING WITH THE INFO
            uiCallback.success(s);
        }

        //PICKER CANCELLED OR generated an error
        else if (resultCode == Activity.RESULT_CANCELED) {
            if (data.getExtras().getBoolean("USER_CANCELLED")) {

                //USER CANCELLED OPERATION.
                uiCallback.failure(new UserCancelledException());

            } else if (data.getExtras().getBoolean("API_ERROR")) {
                //API BASED ERROR. LOAD ERROR INFO
                String apiErrorCode = data.getExtras().getString("API_ERROR_CODE");
                String apiErrorString = data.getExtras().getString("API_ERROR_STRING");
                URL apiErrorURL = (URL) data.getExtras().get("API_ERROR_URL");

                //DO SOMETHING WITH ERROR INFO
                uiCallback.failure(new Exception(apiErrorCode + " ["+apiErrorCode+"]"));

            } else {
                //SYSTEM EXCEPTION. LOAD EXCEPTION
                Exception e = (Exception) data.getExtras().get("SYSTEM_EXCEPTION");

                //HANDLE EXCEPTION
                uiCallback.failure(e);

            }
        }
    }

    public void toast(String message) {
        Toast.makeText(MyActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}
