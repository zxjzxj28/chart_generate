# ChartGenerator - MPAndroidChart 分组柱状图生成器

## 项目说明

基于 MPAndroidChart 的 Android 图表生成工具，可快速生成分组柱状图并保存为 PNG 图片。

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Gradle 8.4
- compileSdk 34
- minSdk 30

## 项目结构

```
ChartGenerator/
├── app/
│   ├── src/main/
│   │   ├── java/com/eagle/android/
│   │   │   └── MainActivity.java      # 主Activity，包含图表配置
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml  # 布局文件
│   │   │   └── values/
│   │   │       ├── colors.xml         # 颜色定义
│   │   │       ├── strings.xml        # 字符串资源
│   │   │       └── themes.xml         # 主题样式
│   │   └── AndroidManifest.xml        # 应用清单
│   ├── build.gradle                   # 模块级构建配置
│   └── proguard-rules.pro             # 混淆规则
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle Wrapper配置
├── build.gradle                       # 项目级构建配置
├── gradle.properties                  # Gradle属性
├── settings.gradle                    # 项目设置
└── README.md                          # 本文件
```

## 快速开始

### 1. 导入项目

用 Android Studio 打开项目根目录即可。

### 2. 修改图表数据

编辑 `MainActivity.java` 中的 **数据配置区域**：

```java
// X轴标签
private final String[] xLabels = {
    "1990年", "1991年", "1992年", ...
};

// 系列名称（图例）
private final String[] seriesNames = {"公司A", "公司B", "公司C"};

// 系列颜色
private final int[] seriesColors = {
    Color.parseColor("#4CD964"),  // 绿色
    Color.parseColor("#5AC8FA"),  // 蓝色
    Color.parseColor("#E8F48C")   // 黄色
};

// 数据值（每行对应一个系列）
private final float[][] seriesData = {
    {10f, 43f, 79f, ...},  // 公司A
    {39f, 7f, 55f, ...},   // 公司B
    {13f, 55f, 33f, ...}   // 公司C
};

// Y轴最大值（0=自动）
private final float yAxisMax = 100f;
```

### 3. 运行与保存

1. 运行 App 后图表自动显示
2. 点击 **保存图表为图片** 按钮
3. 图片保存至 `Pictures/Charts/` 目录

## 自定义扩展

### 添加更多系列

只需在 `seriesNames`、`seriesColors`、`seriesData` 中添加对应数据即可。

### 调整样式

修改 `setupChart()` 方法中的参数：
- 字体大小：`setTextSize()`
- 网格线：`setDrawGridLines()`
- 图例位置：`Legend.setVerticalAlignment()`

## 依赖库

- MPAndroidChart v3.1.0
- AndroidX AppCompat 1.6.1
- Material Components 1.11.0

## 许可证

MIT License
