/**
 * 配色方案配置
 */

export const colorSchemes = [
  {
    id: 'default',
    name: 'WJX默认',
    colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
    preview: 'linear-gradient(90deg, #5470c6 0%, #91cc75 25%, #fac858 50%, #ee6666 75%, #73c0de 100%)'
  },
  {
    id: 'contrast',
    name: '深浅对比',
    colors: ['#1f77b4', '#aec7e8', '#ff7f0e', '#ffbb78', '#2ca02c', '#98df8a', '#d62728', '#ff9896', '#9467bd', '#c5b0d5'],
    preview: 'linear-gradient(90deg, #1f77b4 0%, #aec7e8 20%, #ff7f0e 40%, #ffbb78 60%, #2ca02c 80%, #98df8a 100%)'
  },
  {
    id: 'gradient',
    name: '色彩渐变',
    colors: ['#5470c6', '#73c0de', '#91cc75', '#fac858', '#ee6666', '#fc8452', '#9a60b4', '#ea7ccc', '#3ba272', '#67c23a'],
    preview: 'linear-gradient(90deg, #5470c6 0%, #73c0de 15%, #91cc75 30%, #fac858 45%, #ee6666 60%, #fc8452 75%, #9a60b4 90%, #ea7ccc 100%)'
  },
  {
    id: 'tech-blue',
    name: '科技蓝',
    colors: ['#409EFF', '#66b1ff', '#85c1ff', '#a4d1ff', '#c3e1ff', '#1890ff', '#096dd9', '#0050b3', '#003a8c', '#002766'],
    preview: 'linear-gradient(90deg, #409EFF 0%, #66b1ff 25%, #85c1ff 50%, #a4d1ff 75%, #c3e1ff 100%)'
  },
  {
    id: 'aurora-green',
    name: '极光绿',
    colors: ['#67c23a', '#85ce61', '#95d475', '#a5da89', '#b5e09d', '#52c41a', '#389e0d', '#237804', '#135200', '#092b00'],
    preview: 'linear-gradient(90deg, #67c23a 0%, #85ce61 25%, #95d475 50%, #a5da89 75%, #b5e09d 100%)'
  }
]

/**
 * 根据ID获取配色方案
 * @param {String} id 配色方案ID
 * @returns {Object} 配色方案对象
 */
export function getColorScheme(id) {
  return colorSchemes.find(scheme => scheme.id === id) || colorSchemes[0]
}

/**
 * 从localStorage加载配色方案
 * @returns {Object} 配色方案对象
 */
export function loadColorScheme() {
  try {
    const saved = localStorage.getItem('statistics_color_scheme')
    if (saved) {
      const scheme = getColorScheme(saved)
      if (scheme) return scheme
    }
  } catch (error) {
    console.error('加载配色方案失败:', error)
  }
  return colorSchemes[0]
}

/**
 * 保存配色方案到localStorage
 * @param {String} id 配色方案ID
 */
export function saveColorScheme(id) {
  try {
    localStorage.setItem('statistics_color_scheme', id)
  } catch (error) {
    console.error('保存配色方案失败:', error)
  }
}

