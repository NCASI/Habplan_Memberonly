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

  #Read in saved schedule from habplan run
  sched <- read.csv("./saveSched")
  #Change column headings so that it's easier to work with
  colnames(sched) <- c("id", "StdID", "sched")
  #Remove any blank spaces that may have been brought in from the import
  sched$StdID <- gsub(" ", "", sched$StdID)
  #Find any differences between the shapefile and saved stand ids
  #std.diff <- intersect(site.shp$StdID, sched$std.id)
  #Filter both files so they match
  #sched <- sched %>%
  #  filter(std.id %in% std.diff)
  #site.shp@data <- site.shp@data %>%
  #  filter(site.shp@data[["StdID"]] %in% std.diff)
  #Add the new column
  new.shp <- merge(site.shp, sched, by = "StdID")
  #Save to the working directory
  writeOGR(new.shp, ".", "Site_with_schedule", driver = "ESRI Shapefile",
           overwrite_layer = T)

  return(new.shp)
}

