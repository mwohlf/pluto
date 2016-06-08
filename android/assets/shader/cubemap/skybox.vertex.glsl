
// edges of the cube
attribute vec3 a_position;

// projecting the model into the world space
uniform mat4 u_worldTrans;  

// projecting the world coordinates into view space
uniform mat4 u_projViewTrans; 

// contains the fieldOfView from the camera
uniform mat4 u_projTrans;  

uniform mat4 u_viewTrans;  
 
varying vec3 v_position;

// see: http://ogldev.atspace.co.uk/www/tutorial25/tutorial25.html
void main () {

    // overriding the z with w guarantees that the final z (after perspective 
    // divide by w) will be 1
    gl_Position = vec4(u_projViewTrans * u_worldTrans * vec4(a_position, 1.0)).xyww;
        
    // the pickup position in the cubemap is always the same, the position itself 
    // rotates, the picup won't change
    v_position = a_position; 
}