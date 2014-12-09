#version 120

varying vec4 vertexColor;

void main() {
  gl_FragColor = vertexColor;
  float dist = distance(gl_PointCoord, vec2(0.5, 0.5));
  gl_FragColor.w = clamp(5*(0.5 - dist), 0., 1.);
}
