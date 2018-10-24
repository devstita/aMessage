package kr.devta.amessage;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddChatActivity extends AppCompatActivity {
    private final int PHONE_NUMBER_LENGTH = 11;

    EditText nameEditText;
    EditText phoneEditText;
    Button doneButton;
    Button findFromContactButton;

    String name;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);
        Manager.getInstance().initActivity(this);

        nameEditText = findViewById(R.id.addChat_NameEditText);
        phoneEditText = findViewById(R.id.addChat_PhoneEditText);
        doneButton = findViewById(R.id.addChat_AddDoneButton);
        findFromContactButton = findViewById(R.id.addChat_FindFromContactButton);

        name = Manager.getInstance().NONE;
        phone = Manager.getInstance().NONE;

        phoneEditText.setTransformationMethod(null); // Number Password remove * Character

        doneButton.setOnClickListener(v -> {
            String innerName = nameEditText.getText().toString();
            String innerPhone = phoneEditText.getText().toString().trim();

//                Manager.getInstance().print("AddChatActivity.DoneButtonClicked -> Name: " + innerName + ", Phone: " + innerPhone);

            if (innerName == null || innerName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name Error", Toast.LENGTH_SHORT).show();
                return;
            }

            if (innerPhone == null || innerPhone.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Phone Error", Toast.LENGTH_SHORT).show();
                return;
            }

            if (innerPhone.length() != PHONE_NUMBER_LENGTH || !(innerPhone.startsWith("010") || innerPhone.startsWith("011") || innerPhone.startsWith("012"))) {
                Toast.makeText(getApplicationContext(), "Phone Syntax Error", Toast.LENGTH_SHORT).show();
                return;
            }

            AddChatActivity.this.name = innerName;
            AddChatActivity.this.phone = innerPhone;

            Intent retIntent = new Intent();
            retIntent.putExtra("Name", name);
            retIntent.putExtra("Phone", phone);
            setResult(RESULT_OK, retIntent);
            finish();
        });

        findFromContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(Intent.ACTION_PICK);
                contactIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactIntent, Manager.getInstance().REQUEST_CODE_CONTACT_INTENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.getInstance().REQUEST_CODE_CONTACT_INTENT && resultCode == RESULT_OK) {
            Cursor cursor = getContentResolver().query(data.getData()
                    , new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}
                    , null, null, null);
            cursor.moveToFirst();

            name = cursor.getString(0);
            phone = cursor.getString(1);
            if (!cursor.isClosed()) cursor.close();

            phone = phone.replaceAll("[^0-9]", "");

            if (phone.length() != PHONE_NUMBER_LENGTH)
                Toast.makeText(getApplicationContext(), "Phone Number Length Error..", Toast.LENGTH_SHORT).show();
            else {
                nameEditText.setText(name);
                phoneEditText.setText(phone);
            }
        }
    }
}
