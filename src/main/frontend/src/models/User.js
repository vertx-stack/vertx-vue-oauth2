import JwtDecode from 'jwt-decode'

export default class User {
  static from (token) {
    try {
      console.log('trying to decode: ', token)
      // let obj = JwtDecode(token)

      // todo: call google API and get profile info from the given access_token
      let obj = {
        user_id: 'na',
        admin: 'na',
        email: 'na'
      }

      console.log('result: ', obj)
      return new User(obj)
    } catch (_) {
      return null
    }
  }

  constructor ({ user_id, admin, email }) {
    this.id = user_id // eslint-disable-line camelcase
    this.admin = admin
    this.email = email
  }

  get isAdmin () {
    return this.admin
  }
}
