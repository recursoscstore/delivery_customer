package com.freshfastfood.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.freshfastfood.activity.AddressActivity;
import com.freshfastfood.activity.DepositoBancario;
import com.freshfastfood.activity.HomeActivity;
import com.freshfastfood.activity.PagarconYape;
import com.freshfastfood.activity.PagarenDelivery;
import com.freshfastfood.activity.PaypalActivity;
import com.freshfastfood.activity.RazerpayActivity;
import com.freshfastfood.activity.RecogereLosProductos;
import com.freshfastfood.database.DatabaseHelper;
import com.freshfastfood.database.MyCart;
import com.freshfastfood.model.Address;
import com.freshfastfood.model.AddressData;
import com.freshfastfood.model.PaymentItem;
import com.freshfastfood.model.RestResponse;
import com.freshfastfood.model.User;
import com.freshfastfood.R;
import com.freshfastfood.utils.CustPrograssbar;
import com.freshfastfood.utils.SessionManager;
import com.freshfastfood.retrofit.APIClient;
import com.freshfastfood.retrofit.GetResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;

import static com.freshfastfood.utils.SessionManager.ADDRESS1;
import static com.freshfastfood.utils.SessionManager.CURRUNCY;
import static com.freshfastfood.utils.SessionManager.TAX;
import static com.freshfastfood.utils.Utiles.isSelect;
import static com.freshfastfood.utils.Utiles.seletAddress;


public class OrderSumrryFragment extends Fragment implements GetResult.MyListener {

    @BindView(R.id.lvlordersumry)
    LinearLayout lvlordersumry;
    @BindView(R.id.txt_subtotal)
    TextView txtSubtotal;
    @BindView(R.id.txt_delivery)
    TextView txtDelivery;
    @BindView(R.id.txt_delevritital)
    TextView txtDelevritital;
    @BindView(R.id.txt_total)
    TextView txtTotal;
    @BindView(R.id.btn_cuntinus)
    TextView btnCuntinus;
    @BindView(R.id.lvlone)
    LinearLayout lvlone;
    @BindView(R.id.lvltwo)
    LinearLayout lvltwo;
    @BindView(R.id.txt_changeadress)
    TextView txtChangeadress;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.txt_texo)
    TextView txtTexo;
    @BindView(R.id.txt_tex)
    TextView txtTex;
    // TODO: Rename and change types of parameters
    private String TIME;
    private String DATA;
    private String PAYMENT;
    int TOTAL;
    public static int paymentsucsses = 0;
    public static String TragectionID = "0";
    public static boolean ISORDER = false;
    PaymentItem paymentItem;
    Address Selectaddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TIME = getArguments().getString("TIME");
            DATA = getArguments().getString("DATE");
            PAYMENT = getArguments().getString("PAYMENT");
            paymentItem = (PaymentItem) getArguments().getSerializable("PAYMENTDETAILS");
        }
    }

    DatabaseHelper databaseHelper;
    List<MyCart> myCarts;
    SessionManager sessionManager;
    Unbinder unbinder;
    User user;
    CustPrograssbar custPrograssbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_sumrry, container, false);
        unbinder = ButterKnife.bind(this, view);
        custPrograssbar = new CustPrograssbar();
        databaseHelper = new DatabaseHelper(getActivity());
        sessionManager = new SessionManager(getActivity());
        HomeActivity.getInstance().setFrameMargin(0);
        user = sessionManager.getUserDetails("");

        getAddress();
        myCarts = new ArrayList<>();
        Cursor res = databaseHelper.getAllData();
        if (res.getCount() == 0) {
            Toast.makeText(getActivity(), "NO DATA FOUND", Toast.LENGTH_LONG).show();
        }
        while (res.moveToNext()) {
            MyCart rModel = new MyCart();
            rModel.setID(res.getString(0));
            rModel.setPID(res.getString(1));
            rModel.setImage(res.getString(2));
            rModel.setTitle(res.getString(3));
            rModel.setWeight(res.getString(4));
            rModel.setCost(res.getString(5));
            rModel.setQty(res.getString(6));
            rModel.setDiscount(res.getInt(7));
            myCarts.add(rModel);
        }
        return view;

    }

    private void setJoinPlayrList(LinearLayout lnrView, List<MyCart> myCarts) {

        lnrView.removeAllViews();
        double[] totalAmount = {0};
        DatabaseHelper helper = new DatabaseHelper(getActivity());
        if (myCarts != null && myCarts.size() > 0) {
            for (int i = 0; i < myCarts.size(); i++) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                MyCart cart = myCarts.get(i);
                View view = inflater.inflate(R.layout.custome_sumrry, null);
                ImageView img_icon = view.findViewById(R.id.img_icon);
                TextView txt_title = view.findViewById(R.id.txt_title);
                TextView txt_priceitem = view.findViewById(R.id.txt_priceanditem);
                TextView txt_price = view.findViewById(R.id.txt_price);
                Glide.with(getActivity()).load(APIClient.Base_URL + "/" + cart.getImage()).thumbnail(Glide.with(getActivity()).load(R.drawable.lodingimage)).into(img_icon);
                double res = (Double.parseDouble(cart.getCost()) / 100.0f) * cart.getDiscount();
                res = Integer.parseInt(cart.getCost()) - res;
                txt_priceitem.setText(sessionManager.getStringData(CURRUNCY) + res);
                txt_title.setText("" + cart.getTitle());
                MyCart myCart = new MyCart();
                myCart.setPID(cart.getPID());
                myCart.setImage(cart.getImage());
                myCart.setTitle(cart.getTitle());
                myCart.setWeight(cart.getWeight());
                myCart.setCost(cart.getCost());
                int qrt = helper.getCard(myCart.getPID(), myCart.getCost());
                txt_priceitem.setText(qrt + " articulo x " + sessionManager.getStringData(CURRUNCY) + res);
                double temp = res * qrt;
                txt_price.setText(sessionManager.getStringData(CURRUNCY) + temp);
                totalAmount[0] = totalAmount[0] + temp;
                lnrView.addView(view);
            }
        }
        txtSubtotal.setText(sessionManager.getStringData(CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));
        if (PAYMENT.equalsIgnoreCase(getResources().getString(R.string.pic_myslf))) {
            txtDelivery.setVisibility(View.VISIBLE);
            txtDelevritital.setVisibility(View.VISIBLE);
            txtDelivery.setText(sessionManager.getStringData(CURRUNCY) + "0");
        } else {
            totalAmount[0] = totalAmount[0] + Selectaddress.getDeliveryCharge();
            txtDelivery.setText(sessionManager.getStringData(CURRUNCY) + Selectaddress.getDeliveryCharge());
        }

        double tex = Double.parseDouble(sessionManager.getStringData(TAX));
        txtTexo.setText("Impuesto de servicio(" + tex + "%)");
        tex = (totalAmount[0] / 100.0f) * tex;
        txtTex.setText(sessionManager.getStringData(CURRUNCY) + new DecimalFormat("##.##").format(tex));
        totalAmount[0] = totalAmount[0] + tex;
        txtTotal.setText(sessionManager.getStringData(CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));
        btnCuntinus.setText("Realizar pedido - " + sessionManager.getStringData(CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));
        TOTAL = (int) totalAmount[0];
    }

    private void OrderPlace(JSONArray jsonArray) {
        custPrograssbar.PrograssCreate();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("timesloat", TIME);
            jsonObject.put("ddate", DATA);
            jsonObject.put("total", TOTAL);
            jsonObject.put("p_method", PAYMENT);
            jsonObject.put("address_id", Selectaddress.getId());
            jsonObject.put("tax", sessionManager.getStringData(TAX));
            jsonObject.put("tid", TragectionID);
            jsonObject.put("pname", jsonArray);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().Order((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.ClosePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                Toast.makeText(getActivity(), "" + response.getResponseMsg(), Toast.LENGTH_LONG).show();
                if (response.getResult().equals("true")) {
                    lvlone.setVisibility(View.GONE);
                    lvltwo.setVisibility(View.VISIBLE);
                    databaseHelper.DeleteCard();
                    ISORDER = true;

                }
            } else if (callNo.equalsIgnoreCase("2323")) {
                Gson gson = new Gson();

                    AddressData addressData = gson.fromJson(result.toString(), AddressData.class);
                    if (addressData.getResult().equalsIgnoreCase("true")) {
                        if (addressData.getResultData().size() != 0) {
                            Selectaddress = addressData.getResultData().get(seletAddress);
                            txtAddress.setText(Selectaddress.getHno() + "," + Selectaddress.getSociety() + "," + Selectaddress.getArea() + "," + Selectaddress.getLandmark() + "," + Selectaddress.getName());
                            setJoinPlayrList(lvlordersumry, myCarts);
                        } else {
                            Toast.makeText(getActivity(), "Por favor agregue su dirección ", Toast.LENGTH_LONG).show();

                            AddressFragment fragment = new AddressFragment();
                            HomeActivity.getInstance().callFragment(fragment);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Por favor agregue su dirección ", Toast.LENGTH_LONG).show();

                        AddressFragment fragment = new AddressFragment();
                        HomeActivity.getInstance().callFragment(fragment);
                    }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.txt_changeadress, R.id.btn_cuntinus, R.id.txt_trackorder})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_changeadress:
                isSelect = true;
                AddressFragment fragment = new AddressFragment();
                HomeActivity.getInstance().callFragment(fragment);
                break;
            case R.id.txt_trackorder:
                ClearFragment();
                break;
            case R.id.btn_cuntinus:
                if (PAYMENT.equalsIgnoreCase("Razorpay")) {
                    startActivity(new Intent(getActivity(), RazerpayActivity.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));

                } else if (PAYMENT.equalsIgnoreCase("Paypal")) {
                   startActivity(new Intent(getActivity(), PaypalActivity.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));

                } else if (PAYMENT.equalsIgnoreCase("Pagar con Yape")) {
                    startActivity(new Intent(getActivity(), PagarconYape.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));
                    sendorderServer();

                }else if (PAYMENT.equalsIgnoreCase("Transferencia Bancaria")) {
                    startActivity(new Intent(getActivity(), DepositoBancario.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));
                    sendorderServer();

                }else if (PAYMENT.equalsIgnoreCase("Pagar en Delivery")) {
                    startActivity(new Intent(getActivity(), PagarenDelivery.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));
                    sendorderServer();

                } else if (PAYMENT.equalsIgnoreCase("Recogere los productos")) {
                    startActivity(new Intent(getActivity(), RecogereLosProductos.class).putExtra("amount", TOTAL).putExtra("detail", paymentItem));
                    sendorderServer();
                }

                break;
            default:
                break;
        }
    }

    public void ClearFragment() {
        sessionManager = new SessionManager(getActivity());
        User user1 = sessionManager.getUserDetails("");
        HomeActivity.getInstance().titleChange("Hola " + user1.getName());
        MyOrderFragment homeFragment = new MyOrderFragment();
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction().replace(R.id.fragment_frame, homeFragment).addToBackStack(null).commit();
    }

    private void sendorderServer() {
        Cursor res = databaseHelper.getAllData();
       if (res.getCount() == 0) {
            return;
       }
        if (user.getArea() != null || user.getSociety() != null || user.getHno() != null || user.getMobile() != null) {
            JSONArray jsonArray = new JSONArray();
            while (res.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", res.getString(0));
                    jsonObject.put("pid", res.getString(1));
                    jsonObject.put("image", res.getString(2));
                    jsonObject.put("title", res.getString(3));
                    jsonObject.put("weight", res.getString(4));
                    jsonObject.put("cost", res.getString(5));
                    jsonObject.put("qty", res.getString(6));
                    jsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            OrderPlace(jsonArray);
        } else {
            startActivity(new Intent(getActivity(), AddressActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.getInstance().serchviewHide();
        HomeActivity.getInstance().setFrameMargin(0);
        try {
            if (paymentsucsses == 1) {
                paymentsucsses = 0;
              //  sendorderServer();
            }
            if (sessionManager != null) {
                Selectaddress = sessionManager.getAddress(ADDRESS1);
                if (Selectaddress != null) {
                    txtAddress.setText(Selectaddress.getHno() + "," + Selectaddress.getSociety() + "," + Selectaddress.getArea() + "," + Selectaddress.getLandmark() + "," + Selectaddress.getName());
                    setJoinPlayrList(lvlordersumry, myCarts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAddress() {
        custPrograssbar.PrograssCreate();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getAddress((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "2323");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
