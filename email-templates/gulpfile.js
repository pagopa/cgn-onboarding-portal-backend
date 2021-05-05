const gulp = require('gulp');
const mjml = require('gulp-mjml');

const srcMjml = './src/*.mjml';
const dstMjml = '../src/main/resources/templates/email';

gulp.task('mjml', () =>
    gulp.src([srcMjml])
        .pipe(mjml())
        .pipe(gulp.dest(dstMjml))
);

gulp.task("watch", () => {
    var targets = [srcMjml];
    gulp.watch(targets, ['mjml']);
});

