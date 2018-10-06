/* eslint-disable global-require */
module.exports = {
  get DfpAna() {
    return require('./RNDfpAna').default;
  },
  get DfpAnaBanner() {
    return require('./RNDfpAnaBanner').default;
  },
  get DFPInterstitial() {
    return require('./RNDFPInterstitial').default;
  },
};
