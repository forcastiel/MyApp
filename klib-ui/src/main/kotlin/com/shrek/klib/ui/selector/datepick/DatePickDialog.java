package com.shrek.klib.ui.selector.datepick;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shrek.klib.colligate.DimenUtils;
import com.shrek.klib.ui.CommonUiSetup;
import com.shrek.klib.ui.R;
import com.shrek.klib.ui.selector.datepick.wheel.NumericWheelAdapter;
import com.shrek.klib.ui.selector.datepick.wheel.OnWheelChangedListener;
import com.shrek.klib.ui.selector.datepick.wheel.WheelView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickDialog extends Dialog {
    
    private DatePickMode mode;

    // 起始时间
    private int startYear, startMonth, startDay;

    private int year,month,day;

    private WheelView wheelYear;

    private WheelView wheelMonth;

    private WheelView wheelDay;

    TextView titleView,yearInfoView,monthInfoView,dayInfoView;
    
    private Context mContext;

    private ChooseDateListener chooseDateListener;

    public DatePickDialog(Context context, Date defaultDate, int startYear, int endYear,DatePickMode mode) {
        super(context, R.style.Base_Dialog);
        this.mContext = context;
        this.mode = mode;
        Calendar cal = Calendar.getInstance();
        cal.setTime(defaultDate);
        View view = getPickerView(cal, startYear, endYear);
        setContentView(view);
        setListener(view);

        WindowManager wm = ((Activity) context).getWindowManager();
        Display display = wm.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() - (int) DimenUtils.dpToPx(80, context);
        getWindow().setAttributes(lp);
    }
    
    
    public View getPickerView(Calendar calendar, int startYearVal, int endYear) {
        Calendar todayCal = Calendar.getInstance();
        startYear = startYearVal == -1 ? todayCal.get(Calendar.YEAR) : startYearVal;
        startMonth = todayCal.get(Calendar.MONTH);
        startDay = todayCal.get(Calendar.DAY_OF_MONTH);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_date_pick_layout, null);

        ImageView imgView = (ImageView) view.findViewById(R.id.wheelview_layout_divider_top);
        imgView.setBackgroundColor(CommonUiSetup.INSTANCE.getPramaryColor());
        
        wheelYear = (WheelView) view.findViewById(R.id.year);
        wheelMonth = (WheelView) view.findViewById(R.id.month);
        wheelDay = (WheelView) view.findViewById(R.id.day);
        
        titleView = (TextView)view.findViewById(R.id.wheelview_title_tv);
        yearInfoView = (TextView)view.findViewById(R.id.ddpl_yearInfoView);
        monthInfoView = (TextView)view.findViewById(R.id.ddpl_monthInfoView);
        dayInfoView = (TextView)view.findViewById(R.id.ddpl_dayInfoView);
        
        switch (mode){
            case YEAR_CHOOSE:
                wheelMonth.setVisibility(View.GONE);
                wheelDay.setVisibility(View.GONE);

                monthInfoView.setVisibility(View.GONE);
                dayInfoView.setVisibility(View.GONE);
                break;
            case MONTH_CHOOSE:
                wheelDay.setVisibility(View.GONE);
                dayInfoView.setVisibility(View.GONE);
                break;
            case DAY_CHOOSE:
                break;
        }
        
        NumericWheelAdapter yearAdapter = new NumericWheelAdapter(
                startYear, endYear);
        NumericWheelAdapter monthAdapter = new NumericWheelAdapter(1, 12);
        // 获取最大的天数
        int dayMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        NumericWheelAdapter dayAdapter = new NumericWheelAdapter(1, dayMax);
        // 年，转轮初始化
        wheelYear.setAdapter(yearAdapter);
        // 循环
        wheelYear.setCyclic(true);
        // 当前项
        wheelYear.setCurrentItem(Math.abs(year - startYear));

        // 月，转轮初始化
        wheelMonth.setAdapter(monthAdapter);
        wheelMonth.setCyclic(true);
        wheelMonth.setCurrentItem(month);

        // 日，转轮初始化默认日期为当前日期的第2天
        wheelDay.setAdapter(dayAdapter);
        wheelDay.setCyclic(true);
        wheelDay.setCurrentItem(startDay - 1);

        wheelYear.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                year = Integer
                        .valueOf(wheelYear.getAdapter().getItem(newValue));
                checkMonth();
            }
        });

        wheelMonth.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                month = newValue;
                checkDay();
            }
        });

        wheelDay.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                day = newValue;
            }
        });
        return view;
    }

    private void checkMonth() {
        NumericWheelAdapter adapter = new NumericWheelAdapter(1, 12);
        wheelMonth.setAdapter(adapter);
        checkDay();
    }

    private void checkDay() {
        NumericWheelAdapter adapter = null;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        // 年月不一样
        int dayMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        adapter = new NumericWheelAdapter(1, dayMax);
        wheelDay.setAdapter(adapter);
        if (day > dayMax) {
            // 当前
            wheelDay.setCurrentItem(dayMax - 1);
        } else {
            wheelDay.setCurrentItem(day);
        }
    }

    public String getTimeString() {
        String year = wheelYear.getAdapter()
                .getItem(wheelYear.getCurrentItem());
        String month = wheelMonth.getAdapter().getItem(
                wheelMonth.getCurrentItem());
        String day = wheelDay.getAdapter().getItem(wheelDay.getCurrentItem());
        DecimalFormat df = new DecimalFormat("00");
        month = df.format(Integer.valueOf(month));
        day = df.format(Integer.valueOf(day));
        String str_date = year + "-" + month + "-" + day;
        return str_date;
    }


    private void setListener(View view) {
        final Button btnSure = (Button) view
                .findViewById(R.id.btn_datetime_sure);
        btnSure.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectTime = getTimeString();
                btnSure.setTextColor(0xffffff);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    chooseDateListener.chooseDate(sdf.parse(selectTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });

        Button btnCancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
        btnCancel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleView.setText(title);
    }

    public void setOnChooseDateListener(ChooseDateListener chooseDateListener) {
        this.chooseDateListener = chooseDateListener;
    }

    public interface ChooseDateListener {
        void chooseDate(Date selectTime);
    }

}
