/**
 * 词云图工具封装
 * 基于echarts-wordcloud
 */

/**
 * 创建词云图配置
 * @param {Object} options 配置选项
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

