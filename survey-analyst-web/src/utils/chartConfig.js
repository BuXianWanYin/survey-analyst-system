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

/**
 * 生成交叉分析图表配置
 * @param {String} type 图表类型 (table, horizontalBar, bar, line, heatmap)
 * @param {Object} crossTable 交叉表数据 { rowKey: { colKey: count } }
 * @param {Object} options 其他选项 { question1Title, question2Title, colorScheme }
 * @returns {Object} ECharts配置对象
 */
export function generateCrossChartOption(type, crossTable, options = {}) {
  const { question1Title = '', question2Title = '', colorScheme = null } = options
  const defaultColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  const colors = colorScheme?.colors || defaultColors

  if (!crossTable || Object.keys(crossTable).length === 0) {
    return null
  }

  const rows = Object.keys(crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(crossTable[row]).forEach(col => cols.add(col))
  })
  const colArray = Array.from(cols)

  // 计算总计用于百分比
  let total = 0
  rows.forEach(row => {
    colArray.forEach(col => {
      total += crossTable[row][col] || 0
    })
  })

  switch (type) {
    case 'horizontalBar':
      // 堆叠条形图（横向）
      const series1 = colArray.map((col, colIndex) => {
        const data = rows.map(row => crossTable[row][col] || 0)
        return {
          name: col,
          type: 'bar',
          stack: 'total',
          data: data,
          label: {
            show: true,
            position: 'inside'
          },
          itemStyle: {
            color: colors[colIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: `${question1Title} × ${question2Title}`,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>'
            let total = 0
            params.forEach(param => {
              result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
              total += param.value
            })
            result += '<strong>合计: ' + total + '</strong>'
            return result
          }
        },
        legend: {
          data: colArray,
          bottom: 0
        },
        grid: {
          left: '15%',
          right: '5%',
          top: '10%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: rows,
          axisLabel: {
            formatter: (value) => {
              return value.length > 20 ? value.substring(0, 20) + '...' : value
            }
          }
        },
        series: series1
      }

    case 'bar':
      // 堆叠柱状图（纵向）
      const series2 = colArray.map((col, colIndex) => {
        const data = rows.map(row => crossTable[row][col] || 0)
        return {
          name: col,
          type: 'bar',
          stack: 'total',
          data: data,
          label: {
            show: true,
            position: 'inside'
          },
          itemStyle: {
            color: colors[colIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: `${question1Title} × ${question2Title}`,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>'
            let total = 0
            params.forEach(param => {
              result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
              total += param.value
            })
            result += '<strong>合计: ' + total + '</strong>'
            return result
          }
        },
        legend: {
          data: colArray,
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '5%',
          top: '10%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: rows,
          axisLabel: {
            rotate: 45,
            interval: 0,
            formatter: (value) => {
              return value.length > 15 ? value.substring(0, 15) + '...' : value
            }
          }
        },
        yAxis: {
          type: 'value'
        },
        series: series2
      }

    case 'line':
      // 折线图
      const series3 = colArray.map((col, colIndex) => {
        const data = rows.map(row => crossTable[row][col] || 0)
        return {
          name: col,
          type: 'line',
          data: data,
          smooth: true,
          label: {
            show: true,
            position: 'top'
          },
          itemStyle: {
            color: colors[colIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: `${question1Title} × ${question2Title}`,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>'
            params.forEach(param => {
              result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
            })
            return result
          }
        },
        legend: {
          data: colArray,
          bottom: 0
        },
        grid: {
          left: '10%',
          right: '5%',
          top: '10%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: rows,
          axisLabel: {
            rotate: 45,
            interval: 0,
            formatter: (value) => {
              return value.length > 15 ? value.substring(0, 15) + '...' : value
            }
          }
        },
        yAxis: {
          type: 'value'
        },
        series: series3
      }

    case 'heatmap':
      // 热力图
      const xAxisData = colArray
      const yAxisData = rows
      const heatmapData = []
      
      rows.forEach((row, rowIndex) => {
        xAxisData.forEach((col, colIndex) => {
          const value = crossTable[row][col] || 0
          const percentage = total > 0 ? (value / total * 100) : 0
          heatmapData.push([colIndex, rowIndex, percentage])
        })
      })
      
      return {
        title: {
          text: `${question1Title} × ${question2Title}`,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          position: 'top',
          formatter: function(params) {
            const rowLabel = yAxisData[params.data[1]]
            const colLabel = xAxisData[params.data[0]]
            const actualValue = crossTable[rowLabel][colLabel] || 0
            const percentage = params.data[2]
            return `${rowLabel} × ${colLabel}<br/>数量: ${actualValue}<br/>占比: ${percentage.toFixed(2)}%`
          }
        },
        grid: {
          height: '60%',
          top: '15%',
          left: '10%',
          right: '10%'
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          splitArea: { show: true },
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'category',
          data: yAxisData,
          splitArea: { show: true }
        },
        visualMap: {
          min: 0,
          max: Math.max(...heatmapData.map(d => d[2]), 1),
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '10%',
          inRange: {
            color: ['#E8F4F8', '#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
          },
          text: ['高', '低'],
          textStyle: { color: '#333' }
        },
        series: [{
          name: '交叉分析',
          type: 'heatmap',
          data: heatmapData,
          label: {
            show: true,
            formatter: function(params) {
              return crossTable[yAxisData[params.data[1]]][xAxisData[params.data[0]]] || 0
            },
            color: '#333',
            fontSize: 12,
            fontWeight: 'bold'
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
              borderWidth: 2,
              borderColor: '#409EFF'
            }
          }
        }]
      }

    default:
      return null
  }
}

/**
 * 生成对比分析图表配置
 * @param {String} type 图表类型 (table, bar, horizontalBar, line)
 * @param {Object} compareData 对比数据 [{ optionLabel, group1: { count, percentage }, ... }]
 * @param {Array} groups 对比组数组 ['组1', '组2', ...]
 * @param {Object} options 其他选项 { questionTitle, colorScheme }
 * @returns {Object} ECharts配置对象
 */
export function generateCompareChartOption(type, compareData, groups, options = {}) {
  const { questionTitle = '', colorScheme = null } = options
  const defaultColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9C27B0', '#FF9800', '#00BCD4']
  const colors = colorScheme?.colors || defaultColors

  if (!compareData || !groups || compareData.length === 0 || groups.length === 0) {
    return null
  }

  const optionsList = compareData.map(item => item.optionLabel)

  switch (type) {
    case 'bar':
      // 柱状图（显示百分比）
      const series1 = groups.map((group, groupIndex) => {
        const data = compareData.map(item => {
          const count = item[group]?.count || 0
          const percentage = item[group]?.percentage || 0
          return {
            value: percentage,
            count: count,
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%'
            }
          }
        })
        return {
          name: group,
          type: 'bar',
          data: data,
          itemStyle: {
            color: colors[groupIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: questionTitle,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              const dataItem = param.data
              const count = dataItem.count || 0
              const percentage = dataItem.value || 0
              result += `${param.marker}${param.seriesName}: ${count} (${percentage.toFixed(2)}%)<br/>`
            })
            return result
          }
        },
        legend: {
          data: groups,
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: optionsList,
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '百分比',
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: series1
      }

    case 'horizontalBar':
      // 条形图（横向，显示百分比）
      const series2 = groups.map((group, groupIndex) => {
        const data = compareData.map(item => {
          const count = item[group]?.count || 0
          const percentage = item[group]?.percentage || 0
          return {
            value: percentage,
            count: count,
            label: {
              show: true,
              position: 'right',
              formatter: '{c}%'
            }
          }
        })
        return {
          name: group,
          type: 'bar',
          data: data,
          itemStyle: {
            color: colors[groupIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: questionTitle,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              const dataItem = param.data
              const count = dataItem.count || 0
              const percentage = dataItem.value || 0
              result += `${param.marker}${param.seriesName}: ${count} (${percentage.toFixed(2)}%)<br/>`
            })
            return result
          }
        },
        legend: {
          data: groups,
          top: 30
        },
        grid: {
          left: '10%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '百分比',
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        yAxis: {
          type: 'category',
          data: optionsList,
          axisLabel: {
            interval: 0
          }
        },
        series: series2
      }

    case 'line':
      // 折线图（显示百分比）
      const series3 = groups.map((group, groupIndex) => {
        const data = compareData.map(item => {
          const count = item[group]?.count || 0
          const percentage = item[group]?.percentage || 0
          return {
            value: percentage,
            count: count,
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%'
            }
          }
        })
        return {
          name: group,
          type: 'line',
          data: data,
          smooth: true,
          itemStyle: {
            color: colors[groupIndex % colors.length]
          }
        }
      })
      
      return {
        title: {
          text: questionTitle,
          left: 'center',
          top: 10,
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              const dataItem = param.data
              const count = dataItem.count || 0
              const percentage = dataItem.value || 0
              result += `${param.marker}${param.seriesName}: ${count} (${percentage.toFixed(2)}%)<br/>`
            })
            return result
          }
        },
        legend: {
          data: groups,
          top: 40,
          left: 'center'
        },
        grid: {
          left: '3%',
          right: '4%',
          top: '25%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: optionsList,
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '百分比',
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: series3
      }

    default:
      return null
  }
}

