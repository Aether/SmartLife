package com.example.android.datafrominternet.Account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.datafrominternet.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.litepal.crud.DataSupport;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chart extends AppCompatActivity {

    private PieChart mPieChart;

    private BarChart mBarChart;

    private RadarChart mRadarChart;

    private XAxis xAxis;

    private TextView textView;

    List<String> sorts =new ArrayList<>
            (Arrays.asList("交通出行", "服饰美容", "生活日用", "通讯", "饮食", "其他"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chart.this,AccountList.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<AccountData> data = DataSupport.findAll(AccountData.class);
        float[] sum = new float[7];
        float outcome = 1;
        for (AccountData i : data){
            sum[i.getSort()+1]+=i.getMoney();
            if(outcome==1){
                outcome--;
                continue;
            }
            outcome+=i.getMoney();
        }
        textView= (TextView) findViewById(R.id.tv_sumup);
        textView.setText("Income_sum:"+sum[0]+",outcome_sum"+outcome);

        pieChartActivity(sum);
        barChartActivity(sum);
        radarChartActivity(sum);

    }


    private void pieChartActivity(float[] sum) {

        mPieChart = (PieChart) findViewById(R.id.mPieChart);
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(15, 30, 15, 20);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);
        //绘制中间文字
//        mPieChart.setCenterText(generateCenterSpannableText());
        mPieChart.setExtraOffsets(20.f, 10.f, 20.f, 10.f);

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(50f);
        mPieChart.setTransparentCircleRadius(53f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);
        // 触摸旋转
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        //添加一个选择监听器
        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Intent intent =  new Intent(Chart.this,AccountList.class);
                intent.putExtra("sort",(int)h.getX());
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });



        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        for (int i = 1; i < 7; i++){
            if (sum[i] != 0){
                entries.add(new PieEntry(sum[i], sorts.get(i-1)));
            }
        }

        //设置数据
        setPieChartData(entries);


        //默认动画
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);
    }

    private void barChartActivity(float[] sum) {

        mBarChart = (BarChart) findViewById(R.id.mBarChart);

        mBarChart.getDescription().setEnabled(false);
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setPinchZoom(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawGridBackground(false);

        xAxis = mBarChart.getXAxis();
        xAxis.setDrawGridLines(false);
        String[] emm = {"交通出行", "服饰美容", "生活日用", "通讯", "饮食", "其他"," w"};
        xAxis.setValueFormatter(new XFormattedValue(emm));

        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.animateY(2500);
        mBarChart.getLegend().setEnabled(false);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < sorts.size(); i++) {
            yVals1.add(new BarEntry(i, sum[i+1]));
        }

        BarDataSet set1;

        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "");
            set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
            set1.setDrawValues(false);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            mBarChart.setData(data);
            mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mBarChart.setFitBars(true);
        }

        mBarChart.invalidate();


    }

//    private SpannableString generateCenterSpannableText() {
//
//        SpannableString s = new SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
//        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
//        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
//        return s;
//    }

    private void setPieChartData(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "Account");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        dataSet.setValueLinePart1OffsetPercentage(100.f);
        dataSet.setValueLinePart1Length(0.42f);
        dataSet.setValueLinePart2Length(0.5f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.DKGRAY);
        mPieChart.setData(data);

        // 撤销所有的亮点
//        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private void radarChartActivity(float[] sum){

        mRadarChart = (RadarChart) findViewById(R.id.mRadarChart);

        Description description = new Description();
        description.setText("(*´◡`*)");
        description.setTextSize(15f);
        description.setTextColor(Color.DKGRAY);
        mRadarChart.setDescription(description);
        // 绘制线条宽度，圆形向外辐射的线条
        mRadarChart.setWebLineWidth(1.5f);
        // 内部线条宽度，外面的环状线条
        mRadarChart.setWebLineWidthInner(1.0f);
        // 所有线条WebLine透明度
        mRadarChart.setWebAlpha(100);

        mRadarChart.getYAxis().setEnabled(false);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it

        setRadarChartData(sum);

        XAxis xAxis = mRadarChart.getXAxis();
        // X坐标值字体大小
        xAxis.setTextSize(12f);

        xAxis.setTextColor(Color.DKGRAY);

        String[] emm = {"交通出行", "服饰美容", "生活日用", "通讯", "饮食", "其他"," w"};
        xAxis.setValueFormatter(new XFormattedValue(emm));

        YAxis yAxis = mRadarChart.getYAxis();
        // Y坐标值标签个数
        yAxis.setLabelCount(6, true);
        // Y坐标值字体大小
        yAxis.setTextSize(15f);

        Legend l = mRadarChart.getLegend();
        // 图例位置
        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        // 图例字体样式
        // 图例X间距
        l.setXEntrySpace(2f);
        // 图例Y间距
        l.setYEntrySpace(1f);


    }

    public void setRadarChartData(float[] sum) {

        ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();

        for (int i = 0; i < sorts.size(); i++) {
            entries1.add(new RadarEntry(sum[i+1],i));
            Log.d("test",sum[i+1]+" "+sorts.get(i));
        }

        RadarDataSet set1 = new RadarDataSet(entries1, "Yours");

        set1.setColor(ColorTemplate.VORDIPLOM_COLORS[1]);
        // 是否实心填充区域
        set1.setDrawFilled(true);

        set1.setFillColor(ColorTemplate.VORDIPLOM_COLORS[1]);
        // 数据线条宽度
        set1.setLineWidth(2f);

//        RadarDataSet set2 = new RadarDataSet(yVals2, "Set 2");
//        set2.setColor(ColorTemplate.VORDIPLOM_COLORS[4]);
//        set2.setDrawFilled(true);
//        set2.setLineWidth(2f);


        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);
//        sets.add(set2);

        RadarData data = new RadarData(sets);

        data.setDrawValues(false);

        mRadarChart.setData(data);

        mRadarChart.invalidate();

}



}