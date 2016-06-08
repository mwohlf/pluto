
#ifdef GL_ES
precision mediump float;
#endif

uniform samplerCube u_skybox;

varying vec3 v_position;

// see: http://ogldev.atspace.co.uk/www/tutorial25/tutorial25.html
void main() {
   // no fog, light, just the color of the cubemap
   gl_FragColor = textureCube(u_skybox, v_position);
}