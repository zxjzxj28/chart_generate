package com.eagle.android;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
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
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BarChart barChart;
    private TextView tvChartTitle;
    private TextView tvXAxisLabel;
    private TextView tvYAxisLabel;

    // ==================== 数据配置区域 ====================
    // 修改以下变量即可生成不同的图表

    // X轴标签（年份/类别）
    private final String[] xLabels = {
            "1990年", "1991年", "1992年", "1993年", "1994年",
//            "1995年", "1996年", "1997年", "1998年", "1999年"
    };

    // 图表标题和轴标签
    private final String chartTitle = "公司业绩对比（1990-1994）";
    private final String xAxisLabel = "年份";
    private final String yAxisLabel = "销售额";

    // 数据系列名称（图例显示）
    private final String[] seriesNames = {"公司A", "公司B", "公司C"};

    // 数据系列颜色 (学术论文配色 - 低饱和度、高对比度、适合黑白打印)
    private final int[] seriesColors = {
            Color.parseColor("#2C3E50"),  // 深蓝灰 - 主色调
            Color.parseColor("#E74C3C"),  // 砖红色 - 对比色
            Color.parseColor("#7F8C8D")   // 中性灰 - 辅助色
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
        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvXAxisLabel = findViewById(R.id.tvXAxisLabel);
        tvYAxisLabel = findViewById(R.id.tvYAxisLabel);
        Button btnSavePng = findViewById(R.id.btnSavePng);
        Button btnSavePdf = findViewById(R.id.btnSavePdf);
        Button btnSaveSvg = findViewById(R.id.btnSaveSvg);

        tvChartTitle.setText(chartTitle);
        tvXAxisLabel.setText(xAxisLabel);
        tvYAxisLabel.setText(yAxisLabel);

        setupChart();
        loadChartData();

        btnSavePng.setOnClickListener(v -> saveChartAsPng());
        btnSavePdf.setOnClickListener(v -> saveChartAsPdf());
        btnSaveSvg.setOnClickListener(v -> saveChartAsSvg());
    }

    /**
     * 配置图表基本属性
     */
    private void setupChart() {
        // 基本设置
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.setExtraTopOffset(45f);
        barChart.setExtraBottomOffset(40f);
        barChart.setExtraRightOffset(50f);

        float density = getResources().getDisplayMetrics().density;
        barChart.getDescription().setEnabled(true);
        barChart.getDescription().setText(chartTitle);
        barChart.getDescription().setTextColor(Color.DKGRAY);
        barChart.getDescription().setTextSize(12f);
        barChart.getDescription().setYOffset(-12f * density);
        barChart.post(() -> barChart.getDescription().setPosition(barChart.getWidth() / 2f, 24f * density));

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
            drawChartAnnotations(canvas);

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
            drawChartAnnotations(canvas);

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

    /**
     * 保存图表为SVG矢量图
     */
    private void saveChartAsSvg() {
        try {
            // 生成SVG内容
            String svgContent = generateSvgContent();

            // 生成文件名
            String fileName = outputFilePrefix + "_" + System.currentTimeMillis() + ".svg";

            // 使用MediaStore保存SVG
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Charts");

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    if (writer != null) {
                        writer.write(svgContent);
                        String message = "SVG矢量图已保存!\n路径: Documents/Charts/" + fileName;
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
     * 在导出的画布上绘制标题与轴标签
     */
    private void drawChartAnnotations(Canvas canvas) {
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.DKGRAY);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(48f);
        canvas.drawText(chartTitle, outputWidth / 2f, 60f, titlePaint);

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(36f);

        canvas.drawText(xAxisLabel, outputWidth / 2f, outputHeight - 25f, labelPaint);

        canvas.save();
        canvas.rotate(-90, 40f, outputHeight / 2f);
        canvas.drawText(yAxisLabel, 40f, outputHeight / 2f, labelPaint);
        canvas.restore();
    }

    /**
     * 生成SVG内容
     */
    private String generateSvgContent() {
        StringBuilder svg = new StringBuilder();

        // SVG尺寸和边距
        int width = outputWidth;
        int height = outputHeight;
        int paddingLeft = 80;
        int paddingRight = 150;
        int paddingTop = 90;
        int paddingBottom = 110;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        int groupCount = xLabels.length;
        int seriesCount = seriesNames.length;

        // 计算柱状图参数
        float groupWidth = (float) chartWidth / groupCount;
        float groupSpace = groupWidth * 0.20f;
        float barSpace = groupWidth * 0.02f;
        float barWidth = (groupWidth - groupSpace) / seriesCount - barSpace;

        // SVG头部
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                width, height, width, height));

        // 白色背景
        svg.append(String.format("  <rect width=\"%d\" height=\"%d\" fill=\"white\"/>\n", width, height));

        // 样式定义
        svg.append("  <style>\n");
        svg.append("    .axis-text { font-family: Arial, sans-serif; font-size: 12px; fill: #444444; }\n");
        svg.append("    .legend-text { font-family: Arial, sans-serif; font-size: 11px; fill: #444444; }\n");
        svg.append("    .grid-line { stroke: #E0E0E0; stroke-width: 0.5; }\n");
        svg.append("    .title-text { font-family: Arial, sans-serif; font-size: 16px; font-weight: bold; fill: #333333; }\n");
        svg.append("    .label-text { font-family: Arial, sans-serif; font-size: 13px; fill: #555555; }\n");
        svg.append("  </style>\n");

        // 标题和轴标签
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" class=\"title-text\" text-anchor=\"middle\">%s</text>\n",
                width / 2, paddingTop - 35, escapeXml(chartTitle)));
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" class=\"label-text\" text-anchor=\"middle\">%s</text>\n",
                paddingLeft + chartWidth / 2, paddingTop + chartHeight + 45, escapeXml(xAxisLabel)));
        svg.append(String.format(
                "  <text x=\"%d\" y=\"%d\" class=\"label-text\" text-anchor=\"middle\" transform=\"rotate(-90 %d %d)\">%s</text>\n",
                paddingLeft - 55, paddingTop + chartHeight / 2, paddingLeft - 55, paddingTop + chartHeight / 2, escapeXml(yAxisLabel)));

        // 绘制网格线
        float maxValue = yAxisMax > 0 ? yAxisMax : getMaxValue();
        int gridCount = 5;
        for (int i = 0; i <= gridCount; i++) {
            float y = paddingTop + chartHeight - (chartHeight * i / gridCount);
            svg.append(String.format(
                    "  <line x1=\"%d\" y1=\"%.1f\" x2=\"%d\" y2=\"%.1f\" class=\"grid-line\"/>\n",
                    paddingLeft, y, paddingLeft + chartWidth, y));

            // Y轴标签
            float value = maxValue * i / gridCount;
            svg.append(String.format(
                    "  <text x=\"%d\" y=\"%.1f\" class=\"axis-text\" text-anchor=\"end\">%.0f</text>\n",
                    paddingLeft - 10, y + 4, value));
        }

        // 绘制柱状图
        for (int g = 0; g < groupCount; g++) {
            float groupX = paddingLeft + g * groupWidth + groupSpace / 2;

            for (int s = 0; s < seriesCount; s++) {
                float barX = groupX + s * (barWidth + barSpace);
                float barHeight = (seriesData[s][g] / maxValue) * chartHeight;
                float barY = paddingTop + chartHeight - barHeight;

                String color = String.format("#%06X", (0xFFFFFF & seriesColors[s]));

                svg.append(String.format(
                        "  <rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" fill=\"%s\"/>\n",
                        barX, barY, barWidth, barHeight, color));
            }

            // X轴标签
            float labelX = groupX + (groupWidth - groupSpace) / 2;
            svg.append(String.format(
                    "  <text x=\"%.1f\" y=\"%d\" class=\"axis-text\" text-anchor=\"middle\">%s</text>\n",
                    labelX, paddingTop + chartHeight + 25, xLabels[g]));
        }

        // 绘制图例
        int legendX = width - paddingRight + 20;
        int legendY = paddingTop + 20;
        int legendItemHeight = 25;

        for (int s = 0; s < seriesCount; s++) {
            int itemY = legendY + s * legendItemHeight;
            String color = String.format("#%06X", (0xFFFFFF & seriesColors[s]));

            // 图例色块
            svg.append(String.format(
                    "  <rect x=\"%d\" y=\"%d\" width=\"15\" height=\"15\" fill=\"%s\"/>\n",
                    legendX, itemY, color));

            // 图例文字
            svg.append(String.format(
                    "  <text x=\"%d\" y=\"%d\" class=\"legend-text\">%s</text>\n",
                    legendX + 22, itemY + 12, seriesNames[s]));
        }

        svg.append("</svg>");

        return svg.toString();
    }
    /**
     * 获取数据最大值
     */
    private float getMaxValue() {
        float max = 0;
        for (float[] series : seriesData) {
            for (float value : series) {
                if (value > max) max = value;
            }
        }
        return max * 1.1f; // 留10%余量
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
