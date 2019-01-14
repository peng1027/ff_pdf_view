#import "FfPdfViewPlugin.h"
#import <ff_pdf_view/ff_pdf_view-Swift.h>

@implementation FfPdfViewPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFfPdfViewPlugin registerWithRegistrar:registrar];
}
@end
