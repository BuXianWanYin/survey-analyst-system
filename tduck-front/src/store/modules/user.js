// import api from '@/api'

const state = {
  token: sessionStorage.getItem('token'),
  userInfo: sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null
}

const getters = {
  isLogin: (state) => {
    return state.token
  },
  userInfo: (state) => {
    return state.userInfo
  }
}

const actions = {
  login(context, payload) {
    return new Promise((resolve) => {
      // 登录成功，写入 token 信息
      context.commit('setData', {
        token: payload.token,
        userInfo: payload
      })
      resolve()
    })
  },
  update(context, payload) {
    return new Promise((resolve) => {
      // 登录成功，写入 token 信息
      context.commit('setData', {
        token: state.token,
        userInfo: payload
      })
      resolve()
    })
  },
  logout(context) {
    context.commit('delData')
  }
}

const mutations = {
  setData(state, data) {
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    state.token = data.token
    state.userInfo = data.userInfo
  },
  delData(state) {
    state.token = null
    state.userInfo = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }
}

export default {
  namespaced: true,
  state,
  actions,
  getters,
  mutations
}
