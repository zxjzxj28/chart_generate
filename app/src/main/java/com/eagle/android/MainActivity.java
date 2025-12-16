package com.eagle.android;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BarChart barChart;

    // ==================== 数据配置区域 ====================
    // 修改以下变量即可生成不同的图表

    // X轴标签（年份/类别）
    private final String[] xLabels = {
            "1990年", "1991年", "1992年", "1993年", "1994年",
//            "1995年", "1996年", "1997年", "1998年", "1999年"
    };

    // 数据系列名称（图例显示）
    private final String[] seriesNames = {"公司A", "公司B", "公司C"};

    // 数据系列颜色 (绿色, 蓝色, 黄色)
    private final int[] seriesColors = {
            Color.parseColor("#4CD964"),  // 绿色
            Color.parseColor("#5AC8FA"),  // 蓝色
            Color.parseColor("#E8F48C")   // 黄色
    };

    // 各系列数据值 - 每行对应一个系列，列数需与xLabels长度一致
    private final float[][] seriesData = {
            // 公司A数据
            {10f, 43f, 79f, 82f, 44f},
//            {10f, 43f, 79f, 82f, 44f, 12f, 18f, 30f, 17f, 11f},
            // 公司B数据
            {39f, 7f, 55f, 26f, 80f},
//            , 51f, 58f, 47f, 9f, 40f
            // 公司C数据
            {13f, 55f, 33f, 96f, 42f}
//            61f, 25f, 41f, 48f, 17f
    };

    // Y轴最大值（设为0则自动计算）
    private final float yAxisMax = 100f;

    // 输出图片文件名前缀
    private final String outputFilePrefix = "grouped_bar_chart";

    // 输出图片尺寸（16:9比例）
    private final int outputWidth = 1920;
    private final int outputHeight = 1080;

    // ==================== 数据配置区域结束 ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChart = findViewById(R.id.barChart);
        Button btnSavePng = findViewById(R.id.btnSavePng);
        Button btnSavePdf = findViewById(R.id.btnSavePdf);

        setupChart();
        loadChartData();

        btnSavePng.setOnClickListener(v -> saveChartAsPng());
        btnSavePdf.setOnClickListener(v -> saveChartAsPdf());
    }

    /**
     * 配置图表基本属性
     */
    private void setupChart() {
        // 基本设置
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.setExtraBottomOffset(10f);
        barChart.setExtraRightOffset(50f);

        // X轴设置
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.LTGRAY);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelRotationAngle(0f);
        xAxis.setCenterAxisLabels(true);

        // 左侧Y轴设置
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setAxisMinimum(0f);
        if (yAxisMax > 0) {
            leftAxis.setAxisMaximum(yAxisMax);
        }
        leftAxis.setTextSize(11f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setLabelCount(6, false);

        // 禁用右侧Y轴
        barChart.getAxisRight().setEnabled(false);

        // 图例设置
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setTextSize(11f);
        legend.setTextColor(Color.DKGRAY);
        legend.setFormSize(10f);
        legend.setFormToTextSpace(5f);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);
        legend.setXOffset(10f);
        legend.setYOffset(10f);
    }

    /**
     * 加载图表数据
     */
    private void loadChartData() {
        int groupCount = xLabels.length;
        int seriesCount = seriesNames.length;

        // 创建各系列的数据集
        List<BarDataSet> dataSets = new ArrayList<>();

        for (int s = 0; s < seriesCount; s++) {
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                entries.add(new BarEntry(i, seriesData[s][i]));
            }

            BarDataSet dataSet = new BarDataSet(entries, seriesNames[s]);
            dataSet.setColor(seriesColors[s]);
            dataSet.setDrawValues(false);  // 不显示柱子上的数值
            dataSets.add(dataSet);
        }

        // 创建BarData
        BarData barData = new BarData(dataSets.toArray(new BarDataSet[0]));

        // 分组柱状图参数
        float groupSpace = 0.20f;  // 组间距
        float barSpace = 0.02f;    // 柱间距
        float barWidth = (1f - groupSpace) / seriesCount - barSpace;

        barData.setBarWidth(barWidth);
        barChart.setData(barData);

        // 设置X轴范围
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(groupCount);

        // 分组柱状图
        barChart.groupBars(0f, groupSpace, barSpace);

        // 刷新图表
        barChart.invalidate();
    }

    /**
     * 保存图表为PNG图片
     */
    private void saveChartAsPng() {
        try {
            // 检查图表是否已渲染
            if (barChart.getWidth() <= 0 || barChart.getHeight() <= 0) {
                Toast.makeText(this, "图表尚未渲染完成，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建指定尺寸的Bitmap
            Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            // 保存原始尺寸
            int originalWidth = barChart.getWidth();
            int originalHeight = barChart.getHeight();

            // 临时调整图表尺寸并绘制
            barChart.layout(0, 0, outputWidth, outputHeight);
            barChart.draw(canvas);

            // 恢复原始尺寸
            barChart.layout(0, 0, originalWidth, originalHeight);

            // 生成文件名
            String fileName = outputFilePrefix + "_" + System.currentTimeMillis() + ".png";

            // 使用MediaStore保存图片（适用于Android 10+）
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Charts");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        String message = "PNG已保存!\n路径: Pictures/Charts/" + fileName;
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 保存图表为PDF矢量图
     */
    private void saveChartAsPdf() {
        try {
            // 检查图表是否已渲染
            if (barChart.getWidth() <= 0 || barChart.getHeight() <= 0) {
                Toast.makeText(this, "图表尚未渲染完成，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建PDF文档
            PdfDocument document = new PdfDocument();

            // 创建页面信息（使用配置的尺寸）
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(outputWidth, outputHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            // 获取PDF页面的Canvas
            Canvas canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE);

            // 保存原始尺寸
            int originalWidth = barChart.getWidth();
            int originalHeight = barChart.getHeight();

            // 临时调整图表尺寸并绘制到PDF
            barChart.layout(0, 0, outputWidth, outputHeight);
            barChart.draw(canvas);

            // 恢复原始尺寸
            barChart.layout(0, 0, originalWidth, originalHeight);

            // 结束页面
            document.finishPage(page);

            // 生成文件名
            String fileName = outputFilePrefix + "_" + System.currentTimeMillis() + ".pdf";

            // 使用MediaStore保存PDF（适用于Android 10+）
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Charts");

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        document.writeTo(outputStream);
                        String message = "PDF矢量图已保存!\n路径: Documents/Charts/" + fileName;
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
            }

            // 关闭文档
            document.close();

        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
