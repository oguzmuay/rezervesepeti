package com.rezerve_sepeti.businessPart;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.rezerve_sepeti.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class TablesActivity extends AppCompatActivity{
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;
    private Button newButton;
    private Switch onOffSwitch;
    private TextView tablePcsTextView;
    private int tablePcs;
    private int pageIndex;
    private int numOfPage;
    private ArrayList<Integer> tableChairPcs;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater(); // inflater is usable for linking two file- xml,layout etc.
        menuInflater.inflate(R.menu.optionsmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }// This method is used for linking menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.signout){
            firebaseAuth.signOut();
            startActivity(new Intent(TablesActivity.this,SignInActivity.class));
            finish();
        }else if(item.getItemId() == R.id.debug){
            Toast.makeText(getApplicationContext(),firebaseUser.getUid(),Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }//this method is used for what will be done when something has selected on menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---------------------Initialize------------------
        //Activity hazirlandi ve kullanilicak objeler olusturuldu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        tablePcsTextView = findViewById(R.id.table_pcss);
        //--------------------------------------------------
        //-------------------Get Table Data-----------------
        //Business veritabanindan veriler cekildi ve masa,sandalye verileri esitlendi.
        firebaseFirestore.collection("develop").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Long pcs = (Long) documentSnapshot.get("table_pcs");
                tablePcs = pcs != null ? pcs.intValue() : 0;
                if(documentSnapshot.get("table_chair_count") != null) {
                    tableChairPcs = (ArrayList<Integer>) documentSnapshot.get("table_chair_count");
                } else{
                    tableChairPcs = new ArrayList<>(tablePcs);
                }
                tablePcsTextView.setText(""+tablePcs);
                initTables(tablePcs);
                onOffSwitch.setChecked(documentSnapshot.getBoolean("isOpen"));
            }
            //Basarisiz olunursa masa ve sandelye sayilari 0 olarak ayarlaniyor.
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                tablePcs = 0;
                tableChairPcs = new ArrayList<>();
                initTables(tablePcs);
                tablePcsTextView.setText("" + tablePcs);
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        //---------------------------------------------------
        //Restoranin acik yada kapali oldugunu belirttigi switch tusu.
        //Switch her degistiginde veri tabanina guncelleme yapiliyor.
        onOffSwitch = (Switch)findViewById(R.id.business_table_onOff_switch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String,Object> model = new HashMap<>();
                model.put("isOpen",isChecked);
                firebaseFirestore.collection("develop").document(firebaseAuth.getCurrentUser().getUid()).set(model, SetOptions.merge()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        onOffSwitch.setChecked(!isChecked);
                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //---------------------------------------------------
        //Codes between 126th-131st lines allow to change screen to map
        findViewById(R.id.business_tables_maps_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TablesActivity.this,BusinessMapsActivity.class));
                finish();
            }
        });
        //Codes between 134th-139th lines allow to change screen to res
        findViewById(R.id.business_tables_reserves_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TablesActivity.this,BusinessResActivity.class));
                finish();
            }});
        updateTableNChairPcs();

        //Codes between 143rd-148th lines allow to change screen to dashboard
        findViewById(R.id.business_tables_dash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TablesActivity.this,DashboardActivity.class));
                finish();
            }
        });
    }
    //----------------------------------------------------
    //-----------------------InitTable--------------------------
    //initTable fonksiyonu kullacinin elinde bulunan masalari masa sayisi max 9 olucak sekilde olusturu ve ekrana ekler.
    //Uygulama ilk acildiginda yada kullanici masa sayfasini her degistiginde burasi cagrilir bu sekilde kullaniciya dinamik bir ara yuz sunulur.
    void initTables(int pcs){
        ((GridLayout)findViewById(R.id.business_table_tables)).removeAllViewsInLayout();
        for (int i = 0; i < (Math.min(pcs, 9)); i++) {
            System.out.println(i);
            newButton = new Button(this);
            newButton.setText(i+".Button");
            //ResourcesCompat.getDrawable(res, R.drawable.expand_collapse, null);
            newButton.setBackground(ResourcesCompat.getDrawable(getApplicationContext().getResources(),R.drawable.activity_main_button_border,null));
            /*newButton.setMinWidth();
            newButton.setMaxWidth();
            newButton.setMinHeight();
            newButton.setMaxHeight();*/
            int buttonIndex = i;
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tableButtonClicked(buttonIndex);
                }
            });
            ((GridLayout)findViewById(R.id.business_table_tables)).addView(newButton);
        }
    }
    //----------------------------------------------
    //TODO: Hazirda bulunan sandalye verisini suan tamamen siliyorum,eski listedeki verileri yeni listeye kaydetmem lazim.
    private void updateTableNChairPcs() {
        //TableActivity icerisinde bulunan kaydet tusuna basildiginda masa sayisi textView'den aliniyor int degiskenine donusturuluyor.
        //Eger ki masa sayisi 0 veya ustuyse masa sayisi uzunlugunda sandalye sayilarini temsil eden bir dizi olusturulur ve veri tabanina kayit edilirler.
        findViewById(R.id.business_table_tableSave).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(tablePcsTextView.getText().toString().length() > 0){
                    tablePcs = Integer.parseInt(tablePcsTextView.getText().toString());
                }
                if (tablePcs >= 0){
                    ArrayList<Integer> tempChairArray = (ArrayList<Integer>) tableChairPcs.clone();
                    tableChairPcs = new ArrayList<>();
                    for (int i = 0; i < tablePcs; i++) {
                        if(i <= tempChairArray.size()-1)
                            tableChairPcs.add(tempChairArray.get(i));
                        else
                            tableChairPcs.add(0);
                    }
                    HashMap<String,Object> tableData= new HashMap<>();
                    tableData.put("table_pcs", tablePcs);
                    tableData.put("table_chair_pcs",tableChairPcs);
                    firebaseFirestore.collection("develop").document(firebaseAuth.getCurrentUser().getUid()).set(tableData, SetOptions.merge()).// yoksa ekliyor varsa üzerine yazıyor.
                            addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(),"Masa Sayısı Alınmıştır!"+tablePcs,Toast.LENGTH_LONG).show();
                            initTables(tablePcs);
                            numOfPage = tablePcs / 10 + 1;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Masa Sayısı Alınamamıştır!",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    //----------------------------------------------------
    private void tableButtonClicked(int i){
        //Kullanici elindeki masalarin rezervasyon durumlarini kontrol etmek icin masalara
        //tikladiginda bir sonraki ekrana gecilir ve secili masanin rezervasyon verileri gosterilir.
        System.out.println("Basilan Tus:"+i*pageIndex);
    }


}