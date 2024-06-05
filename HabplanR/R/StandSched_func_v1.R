#' Save top Habplan schedules to shapefile
#'
#' Saves the shedules provided by Habplan to the study site shapefiles
#' @param site.shp SpatVector file of study area (forest stands)
#' @return A .shp file with each stand regime included
#' @examples
#' #Read in stand shapefiles
#' site.shp <- readOGR(dsn = "./shapefiles/Stands2_Shapefile/Stands_final.shp")
#'
#' #Run function
#' new.shp <- standSched(site.shp = site.shp)
#'
#' @export

standSched <- function(site.shp){

  site.shp.2 <- site.shp
  #Read in saved schedule from habplan run
  sched <- read.csv("./saveSched")
  #Change column headings so that it's easier to work with
  colnames(sched) <- c("id", "StdID", "sched")
  #Remove any blank spaces that may have been brought in from the import
  sched$StdID <- gsub(" ", "", sched$StdID)
  #Add the new column
  new.shp <- merge(site.shp.2, sched, by = "StdID")
  #Save to the working directory
  writeVector(new.shp, "./Site_with_schedule.shp", filetype="ESRI Shapefile",
            overwrite=TRUE)
  #writeOGR(new.shp, ".", "Site_with_schedule", driver = "ESRI Shapefile",
  #         overwrite_layer = T)

  return(new.shp)
}

