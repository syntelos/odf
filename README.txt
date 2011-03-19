
Simple translation of ODF for convenience.

  * Requires apache ant and mercurial (hg).

  * Run ./build.sh
      to produce 'odf.jar' containing ODFDOM and SIMPLE.

The SIMPLE package has been renamed from 'org.odftoolkit.simple' to 'odf'.

For example,

      odf.SpreadsheetDocument ods = odf.SpreadsheetDocument.loadDocument(src);

  for 'src' an InputStream, File or string file path name.

See also

  * http://odftoolkit.org/projects/odfdom
  * http://odftoolkit.org/projects/simple
