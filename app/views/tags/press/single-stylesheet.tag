*{
 *  Outputs a <css> tag whose source is the compressed output of the file
 *  specified as a parameter
 *  When the plugin is disabled, outputs a css tag with the original source
 *  for easy debugging.
 *
 *  eg:
 *  #{press.single-stylesheet "widget.css", media:'screen'}
 *
 *  will output:
 *  <link href="/public/stylesheets/press/widget.min.css" rel="stylesheet" type="text/css" charset="utf-8" media="screen"></link>
 *
 *  See the plugin documentation for more information.
 *  
}*
%{
    ( _arg ) &&  ( _src = _arg);
    
    if(! _src) {
        throw new play.exceptions.TagInternalException("src attribute cannot be empty for press.single-stylesheet tag");
    }
}%
${ press.Plugin.addSingleCSS(_src, _media) }