package com.example.tamapbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class Main2Activity extends AppCompatActivity {

    CheckBox asiaAf_checkBox;
    CheckBox geo_checkBox;
    CheckBox siliwa_checkBox;
    CheckBox sri_checkBox;
    CheckBox monumen_checkBox;
    Button btncarirute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        asiaAf_checkBox = findViewById(R.id.MuseumAsiaAfrika);
        geo_checkBox = findViewById(R.id.MuseumGeologi);
        siliwa_checkBox = findViewById(R.id.MuseumSiliwangi);
        sri_checkBox = findViewById(R.id.MuseumSriBaduga);
        monumen_checkBox = findViewById(R.id.MonumenPerjuanganRakyatJawaBarat);

        btncarirute = findViewById(R.id.btncarirute);

        btncarirute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, MainActivity.class);

                intent.putExtra("asiaAf", asiaAf_checkBox.isChecked());
                intent.putExtra("geo", geo_checkBox.isChecked());
                intent.putExtra("siliwa", siliwa_checkBox.isChecked());
                intent.putExtra("sri", sri_checkBox.isChecked());
                intent.putExtra("monumen", monumen_checkBox.isChecked());

                startActivity(intent);
            }
        });

    }
}
