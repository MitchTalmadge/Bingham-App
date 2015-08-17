package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;


public class GPACalcFragment extends Fragment implements View.OnClickListener {

    EditText a, am, bp, b, bm, cp, c, cm, dp, d, dm, f;
    TextView result;
    Button calculate;

    public GPACalcFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gpa_calc, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        a = (EditText) getView().findViewById(R.id.gpa_a);
        am = (EditText) getView().findViewById(R.id.gpa_a_minus);
        bp = (EditText) getView().findViewById(R.id.gpa_b_plus);
        b = (EditText) getView().findViewById(R.id.gpa_b);
        bm = (EditText) getView().findViewById(R.id.gpa_b_minus);
        cp = (EditText) getView().findViewById(R.id.gpa_c_plus);
        c = (EditText) getView().findViewById(R.id.gpa_c);
        cm = (EditText) getView().findViewById(R.id.gpa_c_minus);
        dp = (EditText) getView().findViewById(R.id.gpa_d_plus);
        d = (EditText) getView().findViewById(R.id.gpa_d);
        dm = (EditText) getView().findViewById(R.id.gpa_d_minus);
        f = (EditText) getView().findViewById(R.id.gpa_f);

        result = (TextView) getView().findViewById(R.id.gpa_result);
        calculate = (Button) getView().findViewById(R.id.gpa_calc);

        calculate.setOnClickListener(this);
    }

    private double calculateGPA() {
        int a,am,bp,b,bm,cp,c,cm,dp,d,dm,f;

        a = ((!this.a.getText().toString().equals("")) ? Integer.valueOf(this.a.getText().toString()) : 0);
        am = ((!this.am.getText().toString().equals("")) ? Integer.valueOf(this.am.getText().toString()) : 0);
        bp = ((!this.bp.getText().toString().equals("")) ? Integer.valueOf(this.bp.getText().toString()) : 0);
        b = ((!this.b.getText().toString().equals("")) ? Integer.valueOf(this.b.getText().toString()) : 0);
        bm = ((!this.bm.getText().toString().equals("")) ? Integer.valueOf(this.bm.getText().toString()) : 0);
        cp = ((!this.cp.getText().toString().equals("")) ? Integer.valueOf(this.cp.getText().toString()) : 0);
        c = ((!this.c.getText().toString().equals("")) ? Integer.valueOf(this.c.getText().toString()) : 0);
        cm = ((!this.cm.getText().toString().equals("")) ? Integer.valueOf(this.cm.getText().toString()) : 0);
        dp = ((!this.dp.getText().toString().equals("")) ? Integer.valueOf(this.dp.getText().toString()) : 0);
        d = ((!this.d.getText().toString().equals("")) ? Integer.valueOf(this.d.getText().toString()) : 0);
        dm = ((!this.dm.getText().toString().equals("")) ? Integer.valueOf(this.dm.getText().toString()) : 0);
        f = ((!this.f.getText().toString().equals("")) ? Integer.valueOf(this.f.getText().toString()) : 0);

        int total = a + am + bp + b + bm + cp + c + cm + dp + d + dm + f;
        double gpa = (a*4 + am*3.67 + bp*3.33 + b*3 + bm*2.67 + cp*2.33 + c*2 + cm*1.67 + dp*1.33 + d*1 + dm*0.67 + f*0)/total;
        return round(gpa, 3);
        //return gpa;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == calculate.getId()) {
            try {
                result.setText(String.valueOf(calculateGPA()));
            } catch (NullPointerException e) {
                result.setText("Invalid");
            }
        }
    }
}
