import 'core-js/features/reflect';
import 'zone.js/dist/zone';
import '@angular/localize/init';

if (process.env.ENV === 'production') {
  // Production
} else {
  // Development and test
  Error['stackTraceLimit'] = Infinity;
  require('zone.js/dist/long-stack-trace-zone');
}