import TduckForm from 'tduck-form-generator'
import store from '@/store/index'

export default {
  data() {
    return {
      formKey: null
    }
  },
  created() {
    // 写入值到sessionStorage 给组件使用（标签页独立）
    if (store.state.user.token) {
      sessionStorage.setItem(TduckForm.constant.ACCESS_TOKEN, store.state.user.token)
    }
  }
}
