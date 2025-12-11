/**
 * 显示设置管理
 */

const DEFAULT_SETTINGS = {
  showTable: true,      // 显示表格
  showBar: true,        // 显示条形图
  showAverage: true,    // 显示平均分
  hideEmpty: false,     // 隐藏空选项
  hideSkip: true        // 隐藏跳过项
}

/**
 * 从localStorage加载显示设置
 * @returns {Object} 显示设置对象
 */
export function loadDisplaySettings() {
  try {
    const saved = localStorage.getItem('statistics_display_settings')
    if (saved) {
      const parsed = JSON.parse(saved)
      return { ...DEFAULT_SETTINGS, ...parsed }
    }
  } catch (error) {
    console.error('加载显示设置失败:', error)
  }
  return { ...DEFAULT_SETTINGS }
}

/**
 * 保存显示设置到localStorage
 * @param {Object} settings 显示设置对象
 */
export function saveDisplaySettings(settings) {
  try {
    localStorage.setItem('statistics_display_settings', JSON.stringify(settings))
  } catch (error) {
    console.error('保存显示设置失败:', error)
  }
}

/**
 * 重置显示设置为默认值
 * @returns {Object} 默认显示设置
 */
export function resetDisplaySettings() {
  saveDisplaySettings(DEFAULT_SETTINGS)
  return { ...DEFAULT_SETTINGS }
}

