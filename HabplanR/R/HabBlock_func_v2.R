#' Create block size constraint data
#'
#' Creates block data files from .shp input data
#' @param std.data Forest stand, regime, and outcome information
#' @param std.info Additional stand information
#' @param site.shp SpatVector file of study area (forest stands)
#' @param block.title Name of the to-be saved .txt file
#' @return Saves a .dat file in the working directory ready for import to Habplan
#' @examples
#' #Read in stand and regime information
#' std.data <- read_csv("./fvs_results.csv")
#'
#' #Read in additional stand information - acreage, id, and description
#' std.info <- read_csv("./Stand_info.csv")
#'
#' #Read in stand shapefiles
#' site.shp <- readOGR(dsn = "./shapefiles/Stands2_Shapefile/Stands_final.shp")
#'
#' #Convert to a SpatVector
#' site.shp <- vect(site.shp)
#'
#' #Run function
#' HabBlock(std.data = std.data, std.info = std.info, site.shp = site.shp,
#' block.title = "block2")
#'
#' @export

HabBlock <- function(std.data, std.info, site.shp, block.title){

  #Get each stand name to run through loop
  std.id <- unique(std.data$StandID)

  nby.test <- adjacent(site.shp, pairs = T)
  #Convert to data frame
  nby.test <- data.frame(nby.test)

  #Loop through adjacencies and convert to correct format
  #loop.len <- unique(nby.test$from)
  adj.list <- vector("list", length = length(std.id))
  for (t in 1:length(std.id)) {
    data.1 <- nby.test %>%
      filter(from == t)
    if(length(data.1) == 0){
      next
    }
    #vec.1 <- paste(unique(data.1$from))
    vec.3 <- as.vector(data.1$to)
    for (i in 1:length(vec.3)) {
      num <- as.numeric(vec.3[i])
      vec.3[i] <- std.id[num]
    }

    vec.3 <- toString(vec.3)
    vec.3 <- gsub('[,]', '', vec.3)

    temp.data <- std.info %>%
      filter(std_id == std.id[t])
    new.acre <- as.character(temp.data[1,2])

    if(vec.3 == "NA"){
      vec.3 <- ""
    }

    data <- paste0(std.id[t], ' ', new.acre, ' ', vec.3)
    adj.list[[t]] <- data
  }

  final.data <- unlist(adj.list)
  data.file <- file(paste0("./", block.title, ".txt"))
  writeLines(final.data, data.file)
  close(data.file)
  return(final.data)
}
