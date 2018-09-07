using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Dfp.Ana.RNDfpAna
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNDfpAnaModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNDfpAnaModule"/>.
        /// </summary>
        internal RNDfpAnaModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNDfpAna";
            }
        }
    }
}
