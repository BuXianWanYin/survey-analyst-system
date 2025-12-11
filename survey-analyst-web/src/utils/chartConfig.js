/**
 * 图表配置生成工具
 * 根据图表类型和统计数据生成 ECharts 配置
 */

/**
 * 根据图表类型和统计数据生成 ECharts 配置
 * @param {String} type 图表类型 (pie, ring, bar, line, horizontalBar, wordcloud)
 * @param {Object} statistics 统计数据
 * @param {Object} colorScheme 配色方案
 * @returns {Object} ECharts配置对象
 */
export function generateChartOption(type, statistics, colorScheme = null) {
  const defaultColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  const colors = colorScheme?.colors || defaultColors

  const baseOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    color: colors
  }

  if (!statistics || !statistics.optionStats || statistics.optionStats.length === 0) {
    return null
  }

  const data = statistics.optionStats.map(opt => ({
    value: opt.count || 0,
    name: opt.optionLabel || opt.optionValue
  }))

  switch (type) {
    case 'pie':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          bottom: '5%'
        },
        series: [{
          name: statistics.questionTitle || '',
          type: 'pie',
          radius: '50%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }

    case 'ring':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          bottom: '5%'
        },
        series: [{
          name: statistics.questionTitle || '',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c} ({d}%)'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          data: data
        }]
      }

    case 'bar':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          show: false
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(d => d.name),
          axisLabel: {
            rotate: data.length > 10 ? 45 : 0,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [{
          name: '选择人数',
          type: 'bar',
          data: data.map(d => d.value),
          itemStyle: {
            color: function(params) {
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      }

    case 'horizontalBar':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          show: false
        },
        grid: {
          left: '15%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '人数'
        },
        yAxis: {
          type: 'category',
          data: data.map(d => d.name),
          axisLabel: {
            interval: 0
          }
        },
        series: [{
          name: '选择人数',
          type: 'bar',
          data: data.map(d => d.value),
          itemStyle: {
            color: function(params) {
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      }

    case 'line':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          show: false
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
          data: data.map(d => d.name),
          axisLabel: {
            rotate: data.length > 10 ? 45 : 0,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [{
          name: '选择人数',
          type: 'line',
          smooth: true,
          data: data.map(d => d.value),
          areaStyle: {
            opacity: 0.3
          }
        }]
      }

    case 'wordcloud':
      // 词云图（用于文本题）
      const wordCloudData = statistics.wordCloudData || []
      if (wordCloudData.length === 0) return null

      return {
        tooltip: {
          trigger: 'item'
        },
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          left: 'center',
          top: 'center',
          width: '70%',
          height: '80%',
          sizeRange: [12, 60],
          rotationRange: [-90, 90],
          gridSize: 8,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'bold',
            color: function() {
              return colors[Math.floor(Math.random() * colors.length)]
            }
          },
          emphasis: {
            focus: 'self',
            textStyle: {
              shadowBlur: 10,
              shadowColor: '#333'
            }
          },
          data: wordCloudData.map(item => ({
            name: item.name || item.word,
            value: item.value || item.count
          }))
        }]
      }

    default:
      return baseOption
  }
}

