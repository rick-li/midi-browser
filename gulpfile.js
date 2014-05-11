'use strict';

var gulp = require('gulp');

var $ = require('gulp-load-plugins')();
var jade = require('jade');
// var qrcode = require('qrcode');

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

// gulp.task('qr', function() {
//   qrcode.save('./files/android.png', 'http://duosuccess.qiniudn.com/duosuccessmidi-browser1.2.6.apk');
//   qrcode.save('./files/ios.png', 'https://itunes.apple.com/cn/app/id812021388');
// });

// Connect
gulp.task('connect', $.connect.server({
  root: ['./'],
  port: 9000,
  livereload: true
}));


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