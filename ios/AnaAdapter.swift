import Foundation
//import CryptoSwift
//import MLAIAna
import whisper
import Datametrical
import Mixpanel

import GoogleMobileAds
import AdSupport

@objc(AnaAdapter)
class AnaAdapter: NSObject {
    
    @objc
    static let shared = AnaAdapter()
    
    @objc
    public var baseURL : String = ""
    @objc
    public var age: NSNumber = 0
    @objc
    public var gender: String = ""
    
    static var appID: String?
    
    
    static var userID : String {
        if let id = UserDefaults.standard.string(forKey: "sh.whisper.UserID") {
            return id
        } else {
            let uuid = UUID().uuidString
            UserDefaults.standard.set(uuid, forKey: "sh.whisper.UserID")
            return uuid
        }
    }
    
    static var versionNumber: String {
        return Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0.0.0"
    }
    
    static var anaServicesConfiguration = AnaServicesConfiguration(apiKey: "d2311e74572048a29251f7cdeefce176",
                                                                   version: "ios_\(versionNumber)")
    

    func getAppID(completion: @escaping (String?) -> ()) {
        if let _ = AnaAdapter.appID {
            DispatchQueue.main.async {
                completion(AnaAdapter.appID)
            }
            return
        }

        guard let bundleIdentifier = Bundle.main.bundleIdentifier else {
            DispatchQueue.main.async {
                completion(AnaAdapter.appID)
            }
            return
        }
        
        
        let urlString = "http://itunes.apple.com/lookup?bundleId=\(bundleIdentifier)"
        
        guard let url = URL(string: urlString) else {
            DispatchQueue.main.async {
                completion(AnaAdapter.appID)
            }
            return
        }
        
        let task = URLSession.shared.dataTask(with: url) { (data, response, error) in
            if  let d            = data,
                let jsonOptional = try? JSONSerialization.jsonObject(with: d, options: .allowFragments) as? JSONDictionary,
                let json         = jsonOptional,
                let results      = json["results"] as? [JSONDictionary],
                let ai           = results.first?["trackId"] as? Int {
                let string = String(ai)
                AnaAdapter.appID = string
            }
            
            DispatchQueue.main.async {
                completion(AnaAdapter.appID)
            }
        }
        task.resume()
    }
    
    @objc
    static func augment(request: DFPRequest, adUnit: String, completion: @escaping (DFPRequest, Error?) -> ()) {
//        anaServicesConfiguration.additionalHeaders = ["x-whisper-testyoself": "24"]
        
//        AnaAdapter.shared.getAppID { (applicationIdentifier) in
            AnaDelegateConfiguration.shared.bundleIdentifier = Bundle.main.bundleIdentifier ?? "xBundleIdentifierx"
            
            AnaDelegateConfiguration.shared.logger   = shared
            AnaDelegateConfiguration.shared.adHider  = shared
            AnaDelegateConfiguration.shared.tracking = shared
//            AnaDelegateConfiguration.shared.appID    = applicationIdentifier
        
            WhisperAna.configure(anaServicesConfiguration, userID: userID) {
                let parameters =
                    AnaParameters(idfa: ASIdentifierManager().advertisingIdentifier.uuidString,
                                  userID: userID,
                                  limitAdTracking: false,
                                  latitude: 0.0,
                                  longitude: 0.0,
                                  adUnitID: adUnit,
                                  auctionTimeout: 0,
                                  age: self.shared.age,
                                  gender: self.shared.gender)
                
                WhisperAna.augmentRequest(request, parameters: parameters) { (request, error) in
                    completion(request, error)
                }
            }
//        }
    }
    
}

extension AnaAdapter : AnaTracking {
    
    public func weaverOnlyTrackingEvent(name: String, objectID: String?, objectType: String?, cohort: String?, extra: String?, extraProperties: JSONDictionary) {
        print("[         AnaAdapter          ] : Tracking Event \(name)")
        WeaverOnlyTrackingEvent(name: name, objectID: objectID, objectType: objectType, cohort: cohort, extra: extra, extraProperties: extraProperties).submit()
    }
    
}


extension AnaAdapter: AnaLogging {
    
    func log(_ message: String) {
        print(message)
    }
}

extension AnaAdapter : AnaAdHiding {

    public func setIsShowingMRAID(_ showingMRAID: Bool) {
        print("[         AnaAdapter         ] : Delegate Notified of Showing Mraid = \(showingMRAID)")
    }

    public func hideSingleton() {
        print("[         AnaAdapter         ] : Delegate Notified to Hide")
    }

    public func showSingleton() {
        print("[         AnaAdapter         ] : Delegate Notified to show")
    }

}

