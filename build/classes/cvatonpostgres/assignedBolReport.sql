/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  Keith Harryman
 * Created: Nov 26, 2017
 */
SELECT  * FROM vAssignedBOLsReport WHERE JobID = $P{JobID} ORDER BY BolID;


