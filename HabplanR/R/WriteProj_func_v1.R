#' Create a project file for configurating Habplan
#'
#' Creates a project file which is configured ready for uploading into the Habplan program
#' @param f1.comp Flow component with all user-defined inputs for Habplan run
#' @param f2.comp Flow component with all user-defined inputs for Habplan run
#' @param f3.comp Flow component with all user-defined inputs for Habplan run
#' @param f4.comp Flow component with all user-defined inputs for Habplan run
#' @param f5.comp Flow component with all user-defined inputs for Habplan run
#' @param f6.comp Flow component with all user-defined inputs for Habplan run
#' @param f7.comp Flow component with all user-defined inputs for Habplan run
#' @param f8.comp Flow component with all user-defined inputs for Habplan run
#' @param f9.comp Flow component with all user-defined inputs for Habplan run
#' @param f10.comp Flow component with all user-defined inputs for Habplan run
#' @param block1 Block size component with all user-defined inputs for Habplan run
#' @param block2 Block size component with all user-defined inputs for Habplan run
#' @param block3 Block size component with all user-defined inputs for Habplan run
#' @param block4 Block size component with all user-defined inputs for Habplan run
#' @param block5 Block size component with all user-defined inputs for Habplan run
#' @param block6 Block size component with all user-defined inputs for Habplan run
#' @param block7 Block size component with all user-defined inputs for Habplan run
#' @param block8 Block size component with all user-defined inputs for Habplan run
#' @param block9 Block size component with all user-defined inputs for Habplan run
#' @param block10 Block size component with all user-defined inputs for Habplan run
#' @return An .xml file called "project" saved in the working directory
#' @examples
#'
#' #Overarching information for habplan run:
#' #Get the number of polygons for later
#' npoly <- length(unique(std.data$StandID))
#' #Get the configuration
#' configuration <- read.csv("./hbp/configuration.hbp", sep="")
#' #Remove the unnecessary part of the configuration
#' config <- gsub('[habplan.config=]', '', configuration$habplan.config.nrow.null)
#' #We can override config by using the following:
#' #config <- "6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"
#' config <- "4,0,0,0,0,0,0,0,0,0,0,0"
#'
#' #Info for f1 component
#' #f1.file is the flow file for the first component
#' f1.file <- "RCW/RCW/Flows/HSI.dat"
#' #f1.bygone
#' f1.bygone <- ""
#' #f1.time0
#' f1.time0 <- "10000"
#' #f1.goal0
#' f1.goal0 <- "0.1"
#' #f1.thlo
#' f1.thlo <- "1000"
#' #f1.thhi
#' f1.thhi <- "5000"
#' #f1.goalplus
#' f1.goalplus <- ".05"
#' #f1.goalf
#' f1.goalf <- "0.5"
#' #f1.slope
#' f1.slope <- "0.0"
#' #f1.weightf
#' f1.weightf <- "1.0"
#' #f1.weight0
#' f1.weight0 <- "1.0"
#' #f1.model
#' f1.model <- "1,1000;20,2500;30,5000;"
#' #f1.title
#' f1.title <- "Breed.dat"
#' #Combine f1 components for writing
#' f1.comp <- c('<flow title="F1 Component">',
#'              paste0('<file value="', f1.file, '" />'),
#'              paste0('<bygone value="', f1.bygone, '" />'),
#'              paste0('<time0 value="', f1.time0, '" />'),
#'              paste0('<goal0 value="', f1.goal0, '" />'),
#'              paste0('<threshLo value="', f1.thlo, '" />'),
#'              paste0('<threshHi value="', f1.thhi, '" />'),
#'              paste0('<goalPlus value="', f1.goalplus, '" />'),
#'              paste0('<goalF value="', f1.goalf, '" />'),
#'              paste0('<slope value="', f1.slope, '" />'),
#'              paste0('<weightF value="', f1.weightf, '" />'),
#'              paste0('<weight0 value="', f1.weight0, '" />'),
#'              paste0('<model value="', f1.model, '" />'),
#'              paste0('<title value="', f1.title, '" />'),
#'              '<bounds height="330" width="366" x="553" y="443" />',
#'              "</flow>")
#'
#' #Info for f2 component
#' #f2.file is the flow file for the first component
#' f2.file <- "RCW/RCW/Flows/Acres.dat"
#' #f2.bygone
#' f2.bygone <- ""
#' #f2.time0
#' f2.time0 <- "10000"
#' #f2.goal0
#' f2.goal0 <- "0.1"
#' #f2.thlo
#' f2.thlo <- "1000"
#' #f2.thhi
#' f2.thhi <- "6000"
#' #f2.goalplus
#' f2.goalplus <- ".05"
#' #f2.goalf
#' f2.goalf <- "0.5"
#' #f2.slope
#' f2.slope <- "0.0"
#' #f2.weightf
#' f2.weightf <- "1.0"
#' #f2.weight0
#' f2.weight0 <- "1.0"
#' #f2.model
#' f2.model <- "1,1000;30,1000;"
#' #f2.title
#' f2.title <- "Acres.dat"
#'
#' f2.comp <- c('<flow title="F2 Component">',
#'           paste0('<file value="', f2.file, '" />'),
#'           paste0('<bygone value="', f2.bygone, '" />'),
#'           paste0('<time0 value="', f2.time0, '" />'),
#'           paste0('<goal0 value="', f2.goal0, '" />'),
#'           paste0('<threshLo value="', f2.thlo, '" />'),
#'           paste0('<threshHi value="', f2.thhi, '" />'),
#'           paste0('<goalPlus value="', f2.goalplus, '" />'),
#'           paste0('<goalF value="', f2.goalf, '" />'),
#'           paste0('<slope value="', f2.slope, '" />'),
#'           paste0('<weightF value="', f2.weightf, '" />'),
#'           paste0('<weight0 value="', f2.weight0, '" />'),
#'           paste0('<model value="', f2.model, '" />'),
#'           paste0('<title value="', f2.title, '" />'),
#'           '<bounds height="336" width="369" x="907" y="442" />',
#'           "</flow>")
#'
#' #Info for f3 component ----
#' #f3.file is the flow file for the first component
#' f3.file <- "RCW/RCW/Flows/Dollars.dat"
#' #f3.bygone
#' f3.bygone <- ""
#' #f3.time0
#' f3.time0 <- "10000"
#' #f3.goal0
#' f3.goal0 <- "0.1"
#' #f3.target
#' #f3.target <- "20000"
#' #f3.thlo
#' f3.thlo <- "5000"
#' #f3.thhi
#' f3.thhi <- "5000000"
#' #f3.goalplus
#' f3.goalplus <- ".05"
#' #f3.goalf
#' f3.goalf <- "0.5"
#' #f3.slope
#' f3.slope <- "0.0"
#' #f3.weightf
#' f3.weightf <- "1.0"
#' #f3.weight0
#' f3.weight0 <- "1.0"
#' #f3.model
#' f3.model.1 <- "1,5000;30,5000;"
#' #f3.next.year - sets up the year where the next target is set
#' #f3.next.year <- "20"
#' #f3.next.target - determines what the next target is after the first year
#' #f3.next.target <- 100000
#' #f3.title
#' f3.title <- "Dollars.dat"
#'
#' f3.comp <- c('<flow title="F3 Component">',
#'           paste0('<file value="', f3.file, '" />'),
#'           paste0('<bygone value="', f3.bygone, '" />'),
#'           paste0('<time0 value="', f3.time0, '" />'),
#'           paste0('<goal0 value="', f3.goal0, '" />'),
#'           paste0('<threshLo value="', f3.thlo, '" />'),
#'           paste0('<threshHi value="', f3.thhi, '" />'),
#'           paste0('<goalPlus value="', f3.goalplus, '" />'),
#'           paste0('<goalF value="', f3.goalf, '" />'),
#'           paste0('<slope value="', f3.slope, '" />'),
#'           paste0('<weightF value="', f3.weightf, '" />'),
#'           paste0('<weight0 value="', f3.weight0, '" />'),
#'           #paste0('<model value="1,', f3.target, ';', f3.next.year, ',',
#'           #      f3.next.target, '" />'),
#'           paste0('<title value="', f3.title, '" />'),
#'           '<bounds height="331" width="368" x="1260" y="440" />',
#'           "</flow>")
#'
#' #Info for f4 component ----
#' #f4.file is the flow file for the first component
#' f4.file <- "RCW/RCW/Flows/MgdStab.dat"
#' #f4.bygone
#' f4.bygone <- ""
#' #f4.time0
#' f4.time0 <- "10000"
#' #f4.goal0
#' f4.goal0 <- "0.1"
#' #f4.thlo
#' f4.thlo <- "1000"
#' #f4.thhi
#' f4.thhi <- "2000"
#' #f4.goalplus
#' f4.goalplus <- ".05"
#' #f4.goalf
#' f4.goalf <- "0.5"
#' #f4.slope
#' f4.slope <- "0.0"
#' #f4.weightf
#' f4.weightf <- "1.0"
#' #f4.weight0
#' f4.weight0 <- "1.0"
#' #f4.model
#' f4.model <- "1,1000;30,2000;"
#' #f4.title
#' f4.title <- "MgdStab.dat"
#'
#' f4.comp <- c('<flow title="F4 Component">',
#'           paste0('<file value="', f4.file, '" />'),
#'           paste0('<bygone value="', f4.bygone, '" />'),
#'           paste0('<time0 value="', f4.time0, '" />'),
#'           paste0('<goal0 value="', f4.goal0, '" />'),
#'           paste0('<threshLo value="', f4.thlo, '" />'),
#'           paste0('<threshHi value="', f4.thhi, '" />'),
#'           paste0('<goalPlus value="', f4.goalplus, '" />'),
#'           paste0('<goalF value="', f4.goalf, '" />'),
#'           paste0('<slope value="', f4.slope, '" />'),
#'           paste0('<weightF value="', f4.weightf, '" />'),
#'           paste0('<weight0 value="', f4.weight0, '" />'),
#'           paste0('<model value="', f4.model, '" />'),
#'           paste0('<title value="', f4.title, '" />'),
#'           '<bounds height="327" width="366" x="1613" y="437" />',
#'           "</flow>")
#'
#' #Write project file
#' #Before running the function, set a new working directory so that
#' #the project file is saved into the correct folder
#' setwd("~/Habplan/Habplan3/Habplan3/project/")
#'
#' #Provide each of these flow component objects to the function and run
#' writeProj(f1.comp = f1.comp, f2.comp= f2.comp, f3.comp= f3.comp,
#'           f4.comp= f4.comp)
#' @export

writeProj <- function(f1.comp = "", block1 = "", f2.comp = "", block2 = "",
                      f3.comp = "", block3 = "", f4.comp = "", block4 = "",
                      f5.comp = "", block5 = "", f6.comp = "", block6 = "",
                      f7.comp = "", block7 = "", f8.comp = "", block8 = "",
                      f9.comp = "", block9 = "", f10.comp = "", block10 = ""){

  proj.file <- file("./project.xml")

  writeLines(c('<?xml version="1.0" ?>',
               "<project>",
               "",
               "<general>",
               "<![CDATA[ ",
               "  Maine from FIA data",
               "]]>",
               paste0('<iters value="', iter, '" />'),
               '<units value="" />',
               '<useunits value="false" />',
               paste0('<home value="', wd, '" />'),
               "</general>",
               "",
               "<components>",
               "",
               f1.comp, "", block1, "", f2.comp, "", block2, "",
               f3.comp, "", block3, "", f4.comp, "", block4, "",
               f5.comp, "", block5, "", f6.comp, "", block6, "",
               f7.comp, "", block7, "", f8.comp, "", block8, "",
               f9.comp, "", block9, "", f10.comp, "", block10, "",
               "</components>",
               "",
               "<bestschedule>",
               '<weight value="1.0,1.0,1.0,1.0" />',
               '<state value="0,0,0,0" />',
               "</bestschedule>",
               "",
               "<output>",
               '<importSave value="true" />',
               paste0('<schedFile value="', wd, '/saveSched" check="true" />'),
               paste0('<graphFile value="', wd, '/saveGraph" check="false" />'),
               paste0('<flowFile title="F1" value="', wd, '/saveFlow1" check="true" />'),
               paste0('<blockFile title="BK1" value="', wd, '/saveBlock1" check="true" />'),
               paste0('<flowFile title="F2" value="', wd, '/saveFlow2" check="true" />'),
               paste0('<blockFile title="BK2" value="', wd, '/saveBlock2" check="true" />'),
               paste0('<flowFile title="F3" value="', wd, '/saveFlow3" check="true" />'),
               paste0('<blockFile title="BK3" value="', wd, '/saveBlock3" check="true" />'),
               paste0('<flowFile title="F4" value="', wd, '/saveFlow4" check="true" />'),
               paste0('<blockFile title="BK4" value="', wd, '/saveBlock4" check="true" />'),
               paste0('<flowFile title="F5" value="', wd, '/saveFlow5" check="true" />'),
               paste0('<blockFile title="BK5" value="', wd, '/saveBlock5" check="true" />'),
               paste0('<flowFile title="F6" value="', wd, '/saveFlow6" check="true" />'),
               paste0('<blockFile title="BK6" value="', wd, '/saveBlock6" check="true" />'),
               paste0('<flowFile title="F7" value="', wd, '/saveFlow7" check="true" />'),
               paste0('<blockFile title="BK7" value="', wd, '/saveBlock7" check="true" />'),
               paste0('<flowFile title="F8" value="', wd, '/saveFlow8" check="true" />'),
               paste0('<blockFile title="BK8" value="', wd, '/saveBlock8" check="true" />'),
               paste0('<flowFile title="F9" value="', wd, '/saveFlow9" check="true" />'),
               paste0('<blockFile title="BK9" value="', wd, '/saveBlock9" check="true" />'),
               paste0('<flowFile title="F10" value="', wd, '/saveFlow10" check="true" />'),
               paste0('<blockFile title="BK10" value="', wd, '/saveBlock10" check="true" />'),

               '<bounds height="262" width="473" x="437" y="317" />',
               "</output>",
               "",
               '<schedule>',
               paste0('<config value="', config, '" />'),
               paste0('<npolys value="', npoly, '" />'),
               '<objective value="1,0,0,0,0,0,0,0,0,0,0" />',
               #paste0(id.list[1:length(id.list)]),
               '<import value="false" />',
               "</schedule>",
               "",
               "</project>",
               ""), proj.file)

  close(proj.file)
}
