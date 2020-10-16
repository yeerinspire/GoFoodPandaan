const functions = require('firebase-functions');
const hmac_sha256 = require('crypto-js/hmac-sha256');
const request = require('request');
const admin = require('firebase-admin');

const serviceAccount = require('./service-account-key.json');
const firebaseConfig = JSON.parse(process.env.FIREBASE_CONFIG);
firebaseConfig.credential = admin.credential.cert(serviceAccount);
admin.initializeApp(firebaseConfig);

exports.getCustomToken = functions.https.onRequest((req,res)=>{
  const accessToken = req.query.access_token;

})
