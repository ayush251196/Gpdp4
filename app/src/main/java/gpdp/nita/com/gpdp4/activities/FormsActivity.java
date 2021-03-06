package gpdp.nita.com.gpdp4.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gpdp.nita.com.gpdp4.R;
import gpdp.nita.com.gpdp4.adapters.FormsAdapter;
import gpdp.nita.com.gpdp4.helpers.DatabaseHelper;
import gpdp.nita.com.gpdp4.helpers.MyLinearLayoutManager;
import gpdp.nita.com.gpdp4.helpers.Utility;
import gpdp.nita.com.gpdp4.interfaces.OnDependentSpinnerItemSelected;
import gpdp.nita.com.gpdp4.interfaces.OnFormsEndListener;
import gpdp.nita.com.gpdp4.interfaces.OnValuesEnteredListener;
import gpdp.nita.com.gpdp4.interfaces.OnViewModifiedListener;
import gpdp.nita.com.gpdp4.models.FormsModel;
import gpdp.nita.com.gpdp4.repositories.Constants;
import gpdp.nita.com.gpdp4.viewmodel.FormsViewModel;
import gpdp.nita.com.gpdp4.viewmodel.MyCustomViewModelFactory;

public class FormsActivity extends AppCompatActivity {


    private static final int GALLERY_REQUEST_CODE = 130;
    private static final int CAMERA_REQUEST_CODE = 140;
    int cameraOrGallery = 0;  //0-cam
    int profilePicPosition = -1;

    private final HashMap<String, String> tableFormMap = new HashMap<>();

    RecyclerView recyclerView;
    MyLinearLayoutManager linearLayoutManager;
    FormsAdapter adapter;
    FormsViewModel formsViewModel;
    Button mNext, mPrev;
    List<FormsModel> mFormsModels;
    TextView subtitle;

    Button fillStatus;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    CircleImageView mCircleImageView;
    String benCode;

    CircleImageView drawerDp;
    TextView drawerSurveyorCode, drawerName;

    SharedPreferences mAutoValuesSharedPref, benList;
    ProgressDialog progressDialog;

    Animation scaleUp, scaleDown;
    ArrayList<String> incompleteTableNames = null;

//    Object[] answersList;

    Dialog dialog;


    private void getFilteredFormsList(ArrayList<String> tableNames, ArrayList<String> buffer) {
        buffer.clear();
        for (int i = 0; i < tableNames.size(); i++) {
            buffer.add(tableFormMap.get(tableNames.get(i)));
        }
    }

    private void initTableFormMap() {

        tableFormMap.put("gpdp_basic_info_1", "1. Basic info");
        tableFormMap.put("gpdp_desc_family_2", "2. Details of family members");
        tableFormMap.put("gpdp_end_poverty_3", "3. End poverty");
        tableFormMap.put("gpdp_income_generation_4", "4. Income generation");
        tableFormMap.put("gpdp_shg_5", "5. SHG");
        tableFormMap.put("gpdp_loan_status_6", "6. Loan status");
        tableFormMap.put("gpdp_mgnrega_7", "7. MGNREGA");
        tableFormMap.put("gpdp_skill_development_8", "8. Skill development");
        tableFormMap.put("gpdp_zero_hunger_9", "9. Zero hunger");
        tableFormMap.put("gpdp_status_icds_10", "10. ICDS enrollment");
        tableFormMap.put("gpdp_food_source_11", "11. Food items");
        tableFormMap.put("gpdp_agriculture_issues_12", "12. Agricultural issues");
        tableFormMap.put("gpdp_agriculture_issues_extra_14", "13. Agricultural issues extra");
        tableFormMap.put("gpdp_good_health_15", "14. Health and well being");
        tableFormMap.put("gpdp_health_status_16", "15. Health status of last year");
        tableFormMap.put("gpdp_institution", "16. Quality education");
        tableFormMap.put("gpdp_gender_equality_18", "17. Gender equality");
        tableFormMap.put("gpdp_women_empowerment_19", "18. Women empowerment");
        tableFormMap.put("gpdp_drinking_water_status_20", "19. Drinking water status");
        tableFormMap.put("gpdp_domestic_water_status_21", "20. Domestic water status");
        tableFormMap.put("gpdp_toilet_facility_22", "21. Status of toilets");
        tableFormMap.put("gpdp_affordable_clean_energy_23", "22. Affordable and clean energy");
        tableFormMap.put("gpdp_climate_action_24", "23. Climate action and protected condition");
        tableFormMap.put("gpdp_disaster_protected_center_25", "24. Disaster protected center");
        tableFormMap.put("gpdp_life_below_water_26", "25. Life below water");
        tableFormMap.put("gpdp_peace_justice_institution_27", "26. Peace justice institution");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);

        initTableFormMap();

        final ArrayList<String> incompleteFormsName = new ArrayList<>();

        final AlertDialog.Builder unfilledFormsDialog = new AlertDialog.Builder(this);
        unfilledFormsDialog.setTitle("Forms not filled");
        unfilledFormsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        benList = this.getSharedPreferences(Constants.BEN_NAMES_SHARED_PREFS, Context.MODE_PRIVATE);

        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        fillStatus = findViewById(R.id.fill_status);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.form0);
        setTitle(navigationView.getCheckedItem().getTitle());

        fillStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (incompleteTableNames != null) {
                    if (incompleteTableNames.size() != 0) {
                        getFilteredFormsList(incompleteTableNames, incompleteFormsName);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(FormsActivity.this,
                                android.R.layout.simple_list_item_1, incompleteFormsName);
                        unfilledFormsDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int formNumber = Integer.parseInt(incompleteFormsName.get(which).split("\\.")[0].trim()) - 1;
                                boolean formPresent = formsViewModel.loadFormNumber(formNumber);
                                if (formPresent) {
                                    navigationView.setCheckedItem(getId(formNumber));
                                    setTitle(navigationView.getCheckedItem().getTitle());
                                    dialog.dismiss();
                                }
                            }
                        });

                        unfilledFormsDialog.show();
                    } else {
                        //Toast.makeText(FormsActivity.this,"You have filled all the forms",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog = new Dialog(FormsActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mAutoValuesSharedPref = this.getSharedPreferences(Constants.AUTO_VALUES, Context.MODE_PRIVATE);

        if (getIntent().getExtras() != null) {
            benCode = getIntent().getExtras().getString("ben_code");
            DatabaseHelper.ben_code = benCode;
        } else {
            benCodeError();
        }

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setItemViewCacheSize(20);

        mNext = findViewById(R.id.btn_next);
        mPrev = findViewById(R.id.btn_prev);
        toolbar = findViewById(R.id.toolbar);
        subtitle = findViewById(R.id.txt_subtitle);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerDp = navigationView.getHeaderView(0).findViewById(R.id.drawer_dp);
        drawerName = navigationView.getHeaderView(0).findViewById(R.id.drawer_name);
        drawerSurveyorCode = navigationView.getHeaderView(0).findViewById(R.id.drawer_surveyor_code);

        setDrawerHeader();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.form0)
                    return selectNavItem(0, menuItem);
                else if (id == R.id.form1)
                    return selectNavItem(1, menuItem);
                else if (id == R.id.form2)
                    return selectNavItem(2, menuItem);
                else if (id == R.id.form3)
                    return selectNavItem(3, menuItem);
                else if (id == R.id.form4)
                    return selectNavItem(4, menuItem);
                else if (id == R.id.form5)
                    return selectNavItem(5, menuItem);
                else if (id == R.id.form6)
                    return selectNavItem(6, menuItem);
                else if (id == R.id.form7)
                    return selectNavItem(7, menuItem);
                else if (id == R.id.form8)
                    return selectNavItem(8, menuItem);
                else if (id == R.id.form9)
                    return selectNavItem(9, menuItem);
                else if (id == R.id.form10)
                    return selectNavItem(10, menuItem);
                else if (id == R.id.form11)
                    return selectNavItem(11, menuItem);
                else if (id == R.id.form12)
                    return selectNavItem(12, menuItem);
                else if (id == R.id.form13)
                    return selectNavItem(13, menuItem);
                else if (id == R.id.form14)
                    return selectNavItem(14, menuItem);
                else if (id == R.id.form15)
                    return selectNavItem(15, menuItem);
                else if (id == R.id.form16)
                    return selectNavItem(16, menuItem);
                else if (id == R.id.form17)
                    return selectNavItem(17, menuItem);
                else if (id == R.id.form18)
                    return selectNavItem(18, menuItem);
                else if (id == R.id.form19)
                    return selectNavItem(19, menuItem);
                else if (id == R.id.form20)
                    return selectNavItem(20, menuItem);
                else if (id == R.id.form21)
                    return selectNavItem(21, menuItem);
                else if (id == R.id.form22)
                    return selectNavItem(22, menuItem);
                else if (id == R.id.form23)
                    return selectNavItem(23, menuItem);
                else if (id == R.id.form24)
                    return selectNavItem(24, menuItem);
                else if (id == R.id.form25)
                    return selectNavItem(25, menuItem);

                return false;
            }
        });


        formsViewModel = ViewModelProviders.of(this, new MyCustomViewModelFactory(this.getApplication(), benCode))
                .get(FormsViewModel.class);

        Constants.initFormList();

        formsViewModel.getFormsModel().observe(this, new Observer<List<FormsModel>>() {
            @Override
            public void onChanged(@Nullable List<FormsModel> formsModels) {

                recyclerView.stopScroll();

                incompleteTableNames = formsViewModel.setIncompleteForms();

                setFillStatus(incompleteTableNames.size());

                mFormsModels = formsModels;
                adapter = null;
                linearLayoutManager = null;
                adapter = new FormsAdapter(mFormsModels, new OnValuesEnteredListener() {
                    @Override
                    public void onDateSet(String date, int position) {
                        formsViewModel.onDateSet(date, position);
                    }

                    @Override
                    public void onViewRemoved(int position, int category) {
                        formsViewModel.onViewRemoved(position, category);
                    }

                    @Override
                    public void onTyping(String text, int position) {
                        formsViewModel.onTyping(text, position);
                    }

                    @Override
                    public void onRadioButtonChecked(int checkId, int position) {
                        formsViewModel.onRadioButtonSelected(checkId, position);
                    }

                    @Override
                    public void onSpinnerItemSelected(Object key, int position) {
                        formsViewModel.onSpinnerItemSelected(key, position);
                    }

                    @Override
                    public void onProfilePicTapped(CircleImageView circleImageView, int position) {

                        selectImage();
                        mCircleImageView = circleImageView;
                        formsViewModel.onProfilePictureTapped(position);
                        profilePicPosition = position;
                    }

                    @Override
                    public void onProfilePictureFetchOffline(final CircleImageView circleImageView,
                                                             final String imageURL,
                                                             final ProgressBar loading) {

                        byte[] imgBytes = null;

                        String surveyorId = mAutoValuesSharedPref.getString("surveyor_id", "");
                        String path = Environment.getExternalStorageDirectory() + "/gpdp/images/" + surveyorId + "/" + DatabaseHelper.ben_code + ".txt";
                        try {
                            String base64EncodedImage = getStringFromFile(path);
                            imgBytes = Base64.decode(base64EncodedImage, Base64.DEFAULT);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (imgBytes != null) {

                            circleImageView.setBorderColor(Color.parseColor("#e67e22"));

                            Glide
                                    .with(recyclerView.getContext())
                                    .load(imgBytes)
                                    .apply(new RequestOptions()
                                            .error(R.drawable.ic_default_avatar)
                                            .placeholder(R.drawable.ic_default_avatar)
                                            .centerCrop()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true))
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            adapter.fetchProfilePicOnline(circleImageView, imageURL, loading);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            loading.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(circleImageView);

                        } else {
                            adapter.fetchProfilePicOnline(circleImageView, imageURL, loading);
                        }
                    }
                });

                linearLayoutManager = new MyLinearLayoutManager(FormsActivity.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(linearLayoutManager);

                if (fillStatus.getVisibility() != View.VISIBLE) {
                    fillStatus.setVisibility(View.VISIBLE);
                    fillStatus.startAnimation(scaleUp);
                }

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy > 0 && fillStatus.getVisibility() == View.VISIBLE) {
                            fillStatus.startAnimation(scaleDown);
                            fillStatus.setVisibility(View.GONE);
                        } else if (dy < 0 && fillStatus.getVisibility() != View.VISIBLE) {
                            fillStatus.startAnimation(scaleUp);
                            fillStatus.setVisibility(View.VISIBLE);
                        }
                    }
                });


                runLayoutAnimation(recyclerView);

                linearLayoutManager.scrollToPosition(0);

                String subT = formsViewModel.getSubTitle();
                if (subT.trim().equals(""))
                    subtitle.setVisibility(View.GONE);
                else {
                    subtitle.setVisibility(View.VISIBLE);
                    subtitle.setText(subT);
                }
            }
        });

        formsViewModel.setOnViewModifiedListener(new OnViewModifiedListener() {

            @Override
            public void onViewModified(int anchorPosition, int id, String[] tokens) {
                adapter.onRadioButtonSelected(anchorPosition, id, tokens);
            }
        });

        formsViewModel.setOnDependentSpinnerItemSelected(new OnDependentSpinnerItemSelected() {
            @Override
            public void onDependentSpinnerItemSelected(int anchorPosition, int selectionPosition, String[] tokens) {
                adapter.onDependentSpinnerItemChosen(anchorPosition, selectionPosition, tokens);
            }
        });

//        formsViewModel.getOneRow().observe(this, new Observer<ArrayList<Object>>() {
//            @Override
//            public void onChanged(@Nullable ArrayList<Object> list) {
//                if (list != null && list.size() > 0) {
//                    formsViewModel.modify(list);
//                }
//            }
//        });

        formsViewModel.setOnFormsEndListener(new OnFormsEndListener() {
            @Override
            public void onSyncStarted() {
                progressDialog = new ProgressDialog(FormsActivity.this);
                progressDialog.setMessage("Please wait while we sync your data with our servers.");
                progressDialog.show();
            }

            @Override
            public void onFormsEnd(boolean isSuccessful, String errorMessage) {
                if (isSuccessful) {
                    benList.edit()
                            .putString(DatabaseHelper.ben_code, DatabaseHelper.getImageURL(DatabaseHelper.ben_code))
                            .apply();
                    dialog.setContentView(R.layout.layout_success);
                    dialog.findViewById(R.id.success_to_main).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toMain();
                        }
                    });
                } else {
                    dialog.setContentView(R.layout.layout_error);
                    if (!errorMessage.trim().equals("")) {
                        TextView message = dialog.findViewById(R.id.error_message);
                        message.setText(errorMessage);
                    }
                    dialog.findViewById(R.id.error_tomain).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toMain();
                        }
                    });
                }
                if (!dialog.isShowing()) {
                    dialog.show();
                }

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

        initRecyclerView();


        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //formsViewModel.insert(answersList);
                recyclerView.stopScroll();
                formsViewModel.insert();
                int i = formsViewModel.loadNext();
                if (i != -1)
                    navigationView.setCheckedItem(getId(i));
                setTitle(navigationView.getCheckedItem().getTitle());

                adapter.clearCache();
            }
        });


        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.stopScroll();
                int i = formsViewModel.loadPrev();
                if (i != -1)
                    navigationView.setCheckedItem(getId(i));
                setTitle(navigationView.getCheckedItem().getTitle());

                adapter.clearCache();
            }
        });

    }


    private void setFillStatus(int size) {
        if (size == 0) {
            fillStatus.setText("All forms filled");
        } else if (size == 1) {
            fillStatus.setText("1 form unfilled");
        } else {
            fillStatus.setText(size + " forms unfilled");
        }
    }

    @Override
    protected void onDestroy() {
        formsViewModel.closeDatabase();
        super.onDestroy();
    }

    private void toMain() {
        Intent toMain = new Intent(this, MainActivity.class);
        toMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toMain);
        finish();
    }

    private void setDrawerHeader() {
        drawerSurveyorCode.setText(mAutoValuesSharedPref.getString("surveyor_id", ""));
        drawerName.setText(mAutoValuesSharedPref.getString("surveyor_name", ""));
        Glide
                .with(this)
                .setDefaultRequestOptions(new RequestOptions()
                        .error(R.drawable.ic_default_avatar)
                        .placeholder(R.drawable.ic_default_avatar))
                .load(mAutoValuesSharedPref.getString("surveyor_img_url", ""))
                .into(drawerDp);
    }

    private void benCodeError() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.opt_profile) {
            toMain();
            return true;
        } else if (id == R.id.opt_sync) {
            sync();
            return true;
        } else return false;
    }

    private void sync() {
        mNext.callOnClick();
        formsViewModel.onFormsEnd();
    }

    private int getId(int pos) {
        switch (pos) {
            case 0:
                return R.id.form0;
            case 1:
                return R.id.form1;

            case 2:
                return R.id.form2;
            case 3:
                return R.id.form3;

            case 4:
                return R.id.form4;
            case 5:
                return R.id.form5;

            case 6:
                return R.id.form6;
            case 7:
                return R.id.form7;

            case 8:
                return R.id.form8;
            case 9:
                return R.id.form9;

            case 10:
                return R.id.form10;
            case 11:
                return R.id.form11;

            case 12:
                return R.id.form12;
            case 13:
                return R.id.form13;

            case 14:
                return R.id.form14;
            case 15:
                return R.id.form15;

            case 16:
                return R.id.form16;
            case 17:
                return R.id.form17;

            case 18:
                return R.id.form18;
            case 19:
                return R.id.form19;

            case 20:
                return R.id.form20;

            case 21:
                return R.id.form21;
            case 22:
                return R.id.form22;

            case 23:
                return R.id.form23;
            case 24:
                return R.id.form24;

            case 25:
                return R.id.form25;

            default:
                return -1;
        }
    }

    private boolean selectNavItem(int index, MenuItem menuItem) {
        boolean formPresent = formsViewModel.loadFormNumber(index);
        if (formPresent) {

            adapter.clearCache();

            drawerLayout.closeDrawer(GravityCompat.START);
            setTitle(menuItem.getTitle());
            return true;
        } else return false;
    }

    private void initRecyclerView() {

    }


    private void selectImage() {

        final CharSequence[] items = {"Take Photo", "Choose from gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(FormsActivity.this);
        builder.setTitle("Upload profile picture");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (item == 0) {

                    boolean result = Utility.checkPermission(FormsActivity.this, 1, "Permission required"
                            , "GPDP needs camera permission to take photos");
                    cameraOrGallery = 0;
                    if (result) cameraIntent();

                } else if (item == 1) {

                    boolean result = Utility.checkPermission(FormsActivity.this, 0, "Permission required"
                            , "GPDP needs read/write permission to external storage for storing user data");
                    cameraOrGallery = 1;
                    if (result) galleryIntent();

                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (cameraOrGallery == 1)
                        galleryIntent();
                } else {
                    Toast.makeText(FormsActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }

            case Utility.MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (cameraOrGallery == 0)
                        cameraIntent();
                } else {
                    Toast.makeText(FormsActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE)
                onSelectFromGalleryResult(data);
            else if (requestCode == CAMERA_REQUEST_CODE)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

                //sendBitmap(bm);
                String base64 = bitmapToBase64(bm);

                Glide
                        .with(FormsActivity.this)
                        .setDefaultRequestOptions(new RequestOptions()
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .load(bm)
                        .into(mCircleImageView);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToFile(String sFileName, String sBody, String subFolder) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "gpdp/" + subFolder + "/");
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    //cannotCreateDirs();
                }
            }
            File gpxfile = new File(root, sFileName);

            FileWriter writer = new FileWriter(gpxfile);

            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        writeToFile(DatabaseHelper.ben_code + ".txt", encoded, "images/" + mAutoValuesSharedPref
                .getString("surveyor_id", ""));

        return encoded;
    }

    private void sendBitmap(Bitmap bm) {
        //new Upload(this).sendImage(bm, DatabaseHelper.ben_code);
    }

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = null;
        if (data.getExtras() != null) {
            thumbnail = (Bitmap) data.getExtras().get("data");
        }

        //sendBitmap(thumbnail);
        bitmapToBase64(thumbnail);

        Glide
                .with(FormsActivity.this)
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .load(thumbnail)
                .into(mCircleImageView);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_show_up);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure to close all streams.
        fin.close();
        return ret;
    }

}