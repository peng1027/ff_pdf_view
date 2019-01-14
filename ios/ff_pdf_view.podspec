#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'ff_pdf_view'
  s.version          = '0.0.1'
  s.summary          = 'PDF image widget'
  s.description      = <<-DESC
A Flutter plugin for Image widget, which can load PDF file.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Farfetch' => 'derrick.zhu@farfetch.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.swift_version = '4.2'
  s.ios.deployment_target = '10.0'
end
