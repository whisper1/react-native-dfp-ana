
Pod::Spec.new do |s|
  s.name         = "RNDfpAna"
  s.version      = "1.0.0"
  s.summary      = "RNDfpAna"
  s.description  = <<-DESC
                  RNDfpAna
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/author/RNDfpAna.git", :tag => "master" }
  s.source_files  = "RNDfpAna/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  s.dependency "Ana", :path => "~/Desktop/Ana"

end
