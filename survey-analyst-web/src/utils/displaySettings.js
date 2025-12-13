/**
 * 显示设置管理
 * 功能：管理统计页面的显示设置，包括表格、条形图、平均分等显示选项的加载和保存
 */

const DEFAULT_SETTINGS = {
  showTable: true,
  showBar: true,
  showAverage: true,
  hideEmpty: false,
  hideSkip: true
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

