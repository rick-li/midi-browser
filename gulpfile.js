'use strict';

var gulp = require('gulp');

var $ = require('gulp-load-plugins')();
var jade = require('jade');
 var qrcode = require('qrcode');

var fs = require('fs');

gulp.task('jade', function() {
  var locals = JSON.parse(fs.readFileSync('./locals.js'));
  console.log('Compiling jade.');
  jade.renderFile('content.jade', locals.zh, function(err, html) {
    fs.writeFileSync('zh.html', html);
  });
  jade.renderFile('content.jade', locals.en, function(err, html) {
    fs.writeFileSync('en.html', html);
  });
});

 gulp.task('qr', function() {
   qrcode.toFile('./files/android.png', 'https://s3-ap-northeast-1.amazonaws.com/duo-mob/midibrowser-release1.9.apk', {
    color: {
      dark: '#00F',  // Blue dots
      light: '#0000' // Transparent background
    }
}, function(){});
   qrcode.toFile('./files/ios.png', 'https://itunes.apple.com/cn/app/id812021388', {
    color: {
      dark: '#00F',  // Blue dots
      light: '#0000' // Transparent background
    }
}, function(){});
 });

// Connect
gulp.task('connect', function(){
    $.connect.server({
    root: ['./'],
    port: 9000,
    livereload: true
  });
});


// Watch
gulp.task('watch', ['connect'], function() {
  // Watch for changes in `app` folder
  gulp.watch([
    './*.html',
  ], function(event) {
    return gulp.src(event.path)
      .pipe($.connect.reload());
  });

  // Watch .jade files
  gulp.watch(['./*.jade', './locals.js'], ['jade']);
});
