/**
 * 词云图工具封装
 * 功能：基于echarts-wordcloud提供词云图配置生成功能
 */

/**
 * 创建词云图配置
 * @param {Object} options 配置选项
 * @param {string} options.title 图表标题
 * @param {Array} options.data 词云数据数组，格式：[{name: 词语, value: 权重}]
 * @param {string} options.shape 词云形状，可选：circle、cardioid、diamond、triangle-forward、triangle、star
 * @param {Array} options.sizeRange 字体大小范围，默认[12, 60]
 * @param {Array} options.rotationRange 旋转角度范围，默认[-90, 90]
 * @returns {Object} ECharts配置对象
 */
export function createWordCloudChart(options = {}) {
  const {
    title = '',
    data = [],
    shape = 'circle', // 'circle', 'cardioid', 'diamond', 'triangle-forward', 'triangle', 'star'
    sizeRange = [12, 60],
    rotationRange = [-90, 90]
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
        right: null,
        bottom: null,
        sizeRange: sizeRange,
        rotationRange: rotationRange,
        rotationStep: 45,
        gridSize: 8,
        drawOutOfBound: false,
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

