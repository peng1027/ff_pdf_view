import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FfPdfView {
  static const MethodChannel _channel = const MethodChannel('ff_pdf_view');

  static Future<List<File>> renderPdf({@required String pdfFile}) async {
    final result = await _channel.invokeMethod("renderPdf", <String, dynamic>{
      "path": pdfFile,
    });

    List<File> files = [];
    if (result.length > 0) {
      result.forEach((r) {
        files.add(File(r));
      });
    }

    return files;
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

class PDFImage extends StatefulWidget {
  final String sourceFile;
  final double size;

  const PDFImage({Key key, this.sourceFile, this.size}) : super(key: key);

  @override
  PDFImageState createState() => new PDFImageState();
}

class PDFImageState extends State<PDFImage> {
  List<File> files = List<File>();

  @override
  void initState() {
    this._renderPdf();
    super.initState();
  }

  void _renderPdf() async {
    final result = await FfPdfView.renderPdf(pdfFile: this.widget.sourceFile);
    setState(() {
      this.files = result;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (files.isEmpty) {
      return Container();
    }

    return Container(child: Image.file(files[0]));
  }
}
