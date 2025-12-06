/**
 * ECharts工具封装
 * 提供常用图表的配置方法
 */

/**
 * 创建柱状图配置
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createBarChart(options = {}) {
  const {
    title = '',
    xAxisData = [],
    seriesData = [],
    xAxisName = '',
    yAxisName = '',
    color = ['#409EFF']
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      name: xAxisName,
      axisLabel: {
        rotate: xAxisData.length > 10 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: yAxisName
    },
    series: [
      {
        name: title,
        type: 'bar',
        data: seriesData,
        itemStyle: {
          color: function(params) {
            return color[params.dataIndex % color.length]
          }
        }
      }
    ]
  }
}

/**
 * 创建饼图配置
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createPieChart(options = {}) {
  const {
    title = '',
    data = [],
    radius = ['40%', '70%'] // 环形图，改为 ['0%', '70%'] 为普通饼图
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: radius,
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
}

/**
 * 创建折线图配置
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createLineChart(options = {}) {
  const {
    title = '',
    xAxisData = [],
    seriesData = [],
    seriesName = '',
    xAxisName = '',
    yAxisName = '',
    smooth = true
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      name: xAxisName
    },
    yAxis: {
      type: 'value',
      name: yAxisName
    },
    series: [
      {
        name: seriesName,
        type: 'line',
        smooth: smooth,
        data: seriesData,
        areaStyle: {
          opacity: 0.3
        }
      }
    ]
  }
}

/**
 * 创建雷达图配置
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createRadarChart(options = {}) {
  const {
    title = '',
    indicator = [],
    seriesData = []
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {},
    radar: {
      indicator: indicator
    },
    series: [
      {
        name: title,
        type: 'radar',
        data: seriesData
      }
    ]
  }
}

/**
 * 创建词云图配置（需要echarts-wordcloud依赖）
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createWordCloudChart(options = {}) {
  const {
    title = '',
    data = [],
    shape = 'circle',
    sizeRange = [12, 60]
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'wordCloud',
        shape: shape,
        left: 'center',
        top: 'center',
        width: '70%',
        height: '80%',
        sizeRange: sizeRange,
        rotationRange: [-90, 90],
        gridSize: 8,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: 'bold',
          color: function() {
            return 'rgb(' + [
              Math.round(Math.random() * 255),
              Math.round(Math.random() * 255),
              Math.round(Math.random() * 255)
            ].join(',') + ')'
          }
        },
        emphasis: {
          focus: 'self',
          textStyle: {
            shadowBlur: 10,
            shadowColor: '#333'
          }
        },
        data: data
      }
    ]
  }
}

/**
 * 自定义图表配置（允许用户自定义颜色、样式等）
 * @param {Object} baseOption 基础配置
 * @param {Object} customConfig 自定义配置
 * @returns {Object} 合并后的配置
 */
export function customizeChart(baseOption, customConfig = {}) {
  const {
    colors = null,
    titleStyle = null,
    legendStyle = null,
    gridStyle = null,
    tooltipStyle = null
  } = customConfig

  const customized = JSON.parse(JSON.stringify(baseOption))

  // 自定义颜色
  if (colors && Array.isArray(colors)) {
    customized.color = colors
  }

  // 自定义标题样式
  if (titleStyle && customized.title) {
    Object.assign(customized.title, titleStyle)
  }

  // 自定义图例样式
  if (legendStyle && customized.legend) {
    Object.assign(customized.legend, legendStyle)
  }

  // 自定义网格样式
  if (gridStyle && customized.grid) {
    Object.assign(customized.grid, gridStyle)
  }

  // 自定义提示框样式
  if (tooltipStyle && customized.tooltip) {
    Object.assign(customized.tooltip, tooltipStyle)
  }

  return customized
}

/**
 * 创建热力图配置
 * @param {Object} options 配置选项
 * @returns {Object} ECharts配置对象
 */
export function createHeatmapChart(options = {}) {
  const {
    title = '',
    xAxisData = [],
    yAxisData = [],
    data = []
  } = options

  return {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...data.map(d => d[2]), 1),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [
      {
        name: title,
        type: 'heatmap',
        data: data,
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
}

