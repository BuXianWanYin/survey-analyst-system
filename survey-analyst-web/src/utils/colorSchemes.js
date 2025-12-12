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
  },
  {
    id: 'warm-orange',
    name: '温暖橙',
    colors: ['#ff9800', '#ffb74d', '#ffcc80', '#ffe0b2', '#fff3e0', '#f57c00', '#e65100', '#bf360c', '#870000', '#3e2723'],
    preview: 'linear-gradient(90deg, #ff9800 0%, #ffb74d 25%, #ffcc80 50%, #ffe0b2 75%, #fff3e0 100%)'
  },
  {
    id: 'purple-dream',
    name: '紫色梦境',
    colors: ['#9c27b0', '#ba68c8', '#ce93d8', '#e1bee7', '#f3e5f5', '#7b1fa2', '#6a1b9a', '#4a148c', '#38006b', '#1a0033'],
    preview: 'linear-gradient(90deg, #9c27b0 0%, #ba68c8 25%, #ce93d8 50%, #e1bee7 75%, #f3e5f5 100%)'
  },
  {
    id: 'ocean-blue',
    name: '海洋蓝',
    colors: ['#00bcd4', '#26c6da', '#4dd0e1', '#80deea', '#b2ebf2', '#0097a7', '#00838f', '#006064', '#004d40', '#001f1f'],
    preview: 'linear-gradient(90deg, #00bcd4 0%, #26c6da 25%, #4dd0e1 50%, #80deea 75%, #b2ebf2 100%)'
  },
  {
    id: 'sunset-red',
    name: '夕阳红',
    colors: ['#f44336', '#ef5350', '#e57373', '#ef9a9a', '#ffcdd2', '#d32f2f', '#c62828', '#b71c1c', '#8b0000', '#4a0000'],
    preview: 'linear-gradient(90deg, #f44336 0%, #ef5350 25%, #e57373 50%, #ef9a9a 75%, #ffcdd2 100%)'
  },
  {
    id: 'forest-green',
    name: '森林绿',
    colors: ['#4caf50', '#66bb6a', '#81c784', '#a5d6a7', '#c8e6c9', '#388e3c', '#2e7d32', '#1b5e20', '#0d3e0d', '#001f00'],
    preview: 'linear-gradient(90deg, #4caf50 0%, #66bb6a 25%, #81c784 50%, #a5d6a7 75%, #c8e6c9 100%)'
  },
  {
    id: 'golden-yellow',
    name: '金黄',
    colors: ['#ffc107', '#ffca28', '#ffd54f', '#ffe082', '#fff59d', '#ffb300', '#ffa000', '#ff8f00', '#ff6f00', '#ff3d00'],
    preview: 'linear-gradient(90deg, #ffc107 0%, #ffca28 25%, #ffd54f 50%, #ffe082 75%, #fff59d 100%)'
  },
  {
    id: 'pink-rose',
    name: '粉玫瑰',
    colors: ['#e91e63', '#ec407a', '#f06292', '#f48fb1', '#f8bbd0', '#c2185b', '#ad1457', '#880e4f', '#560027', '#2d0014'],
    preview: 'linear-gradient(90deg, #e91e63 0%, #ec407a 25%, #f06292 50%, #f48fb1 75%, #f8bbd0 100%)'
  },
  {
    id: 'teal-cyan',
    name: '青绿',
    colors: ['#009688', '#26a69a', '#4db6ac', '#80cbc4', '#b2dfdb', '#00796b', '#00695c', '#004d40', '#00251a', '#000d0a'],
    preview: 'linear-gradient(90deg, #009688 0%, #26a69a 25%, #4db6ac 50%, #80cbc4 75%, #b2dfdb 100%)'
  },
  {
    id: 'indigo-deep',
    name: '深靛蓝',
    colors: ['#3f51b5', '#5c6bc0', '#7986cb', '#9fa8da', '#c5cae9', '#303f9f', '#283593', '#1a237e', '#0d0f4a', '#05061f'],
    preview: 'linear-gradient(90deg, #3f51b5 0%, #5c6bc0 25%, #7986cb 50%, #9fa8da 75%, #c5cae9 100%)'
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

