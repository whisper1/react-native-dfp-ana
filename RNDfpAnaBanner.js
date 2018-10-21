import React, { Component } from 'react';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
  ViewPropTypes,
  NativeModules
} from 'react-native';
import {
  string,
  func,
  arrayOf
} from 'prop-types';

class DfpAnaBanner extends Component {
  constructor() {
    super();
    this.handleSizeChange = this.handleSizeChange.bind(this);
    this.handleAppEvent = this.handleAppEvent.bind(this);
    this.handleAdFailedToLoad = this.handleAdFailedToLoad.bind(this);
    this.state = {
      style: {},
    };
  }

  componentDidMount() {
    this.loadBanner();
  }

  componentWillUnmount() {
    this.destroyBanner();
  }

  loadBanner() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this._bannerView),
      UIManager.RNDfpAnaBannerView.Commands.loadBanner,
      null,
    );
  }

  resumeBanner() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this._bannerView),
      UIManager.RNDfpAnaBannerView.Commands.resumeBanner,
      null,
    );
  }

  pauseBanner() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this._bannerView),
      UIManager.RNDfpAnaBannerView.Commands.pauseBanner,
      null,
    );
  }

  destroyBanner() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this._bannerView),
      UIManager.RNDfpAnaBannerView.Commands.destroyBanner,
      null,
    );
  }

  handleSizeChange(event) {
    const { height, width } = event.nativeEvent;
    this.setState({ style: { width, height } });
    if (this.props.onSizeChanged) {
      this.props.onSizeChanged({ width, height });
    }
  }

  handleAppEvent(event) {
    if (this.props.onAppEvent) {
      const { name, info } = event.nativeEvent;
      this.props.onAppEvent({ name, info });
    }
  }

  handleAdFailedToLoad(event) {
    if (this.props.onAdFailedToLoad) {
      this.props.onAdFailedToLoad(event.nativeEvent.error);
    }
  }

  render() {
    return (
      <RNDfpAnaBannerView
        {...this.props}
        style={[this.props.style, this.state.style]}
        onSizeChanged={this.handleSizeChange}
        onAdFailedToLoad={this.handleAdFailedToLoad}
        onAppEvent={this.handleAppEvent}
        ref={el => (this._bannerView = el)}
      />
    );
  }
}

DfpAnaBanner.simulatorId = 'SIMULATOR';

DfpAnaBanner.propTypes = {
  ...ViewPropTypes,

  adSize: string, // Only 'banner' is supported currently
  validAdSizes: arrayOf(string),
  adUnitID: string,
  testDevices: arrayOf(string),
  onSizeChanged: func,
  onAdLoaded: func,
  onAdFailedToLoad: func,
  onAdOpened: func,
  onAdClosed: func,
  onAdLeftApplication: func,
  onAppEvent: func,
};

const RNDfpAnaBannerView = requireNativeComponent('RNDfpAnaBannerView', DfpAnaBanner);
// module.exports = requireNativeComponent('RNDfpAnaBannerView', null);

export default DfpAnaBanner;
