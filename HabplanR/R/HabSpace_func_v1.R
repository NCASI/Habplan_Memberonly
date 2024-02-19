#' Landscape metric analyses on study site using Habplan output file
#'
#' Applies various landscape metric analyses on study site using Habplan output file
#' @param site.shp SpatVector file of study area (forest stands)
#' @param flow Output flow file of interest (typically habitat area)
#' @param nyear Number of years or time periods interested in (have data for)
#' @param mode "terrestrial" or "avian" depending on movement type of study species
#' @param dist Dispersal distance of species if no habitat exists. I.e., the possible barrier width that can be traversed by an avian species.
#' @param level Levels based on the "Landscapemetrics" package - "patch", "landscape", or "class"
#' @return FIgures of landscapes over time, csv files of landscape metrics, shapefiles of all stands over time
#' @examples
#' #Read in stand shapefiles
#' site.shp <- readOGR(dsn = "./shapefiles/Stands2_Shapefile/Stands_final.shp")
#'
#' #Read in flow file from Habplan run
#' flow <- read.csv("./saveFlow1.dat")
#'
#' ##Important: The shapefile needs a data column called "StdID"##
#'
#' #We will add the shapefile of stands, and the flow of choice to the
#' #function. As before, we will set the nyear. We now have two more options
#' #with this function, mode and dist.
#'
#' #There are two modes to choose from, terrestrial or avian - depending
#' #on the species the HSI has been built around. If avian is chosen, the
#' #dist setting sets the distance in meters (or the unit of the shapefile)
#' #that the species of interest can comfortably travel to reach neighboring
#' #habitat patches. If the user has a terrestrial species which may still
#' #bridge gaps if the distance is short enough, e.g. crossing a road, then
#' #the avian option should be selected, and a smaller dist assigned.
#'
#' #Want to create a shapefile output for each year, to look at change
#' #of regime for each stand over time.
#'
#' test.x <- HabSpace(site.shp = site.shp, flow = flow, nyear = 35,
#'                  mode = "terrestrial", dist = 500, level = "landscape")
#'
#'
#' @export

HabSpace <- function(site.shp = site.shp, flow = flow, nyear = nyear,
                     mode = "terrestrial", dist = 500, level = "patch"){

  #Format flow file to remove commas and extra column
  for (i in 1:ncol(flow)) {
    flow[,i] <- gsub(",", "", flow[,i])
  }
  flow <- flow[,-1]
  
  #Rename column headings of flow file to apply to any flow file input
colnames(flow) <- c("std_id", 1:(ncol(flow)-1))

#Sort shapefile data by stand id
shp_df <- sf::st_as_sf(site.shp)
#Merge this with the flow file
shp.new <- merge(shp_df, flow, by.x="StdID", by.y="std_id" , all.x = TRUE)
#nyear <- 35
flow.list.1 <- vector(mode = "list", length = nyear)
col.num <- ncol(shp_df)

#This loop creates a list of stand id, year, and flow information
for (p in 1:length(flow.list.1)) {
  std <- data.frame(cbind(shp.new[,1], shp.new[,p+col.num], shp.new[,p+col.num+nyear]))
  colnames(std) <- c("id", "year", "flow")
  std <- std[c(1:3)]
  std$flow <- as.numeric(std$flow)
  std <- as.data.frame(std)
  flow.list.1[[p]] <- std
}

#Now combine them
mast.std <- bind_rows(flow.list.1)
#Remove column_label id
#mast.std = subset(mast.std, select = -c(column_label) )
#Create replicates of shapefile data for each year for plotting later
temp.data <- do.call("rbind", replicate(35, shp.new, simplify = FALSE))
#Create master data of all information
new.data <- cbind(mast.std, temp.data)
#Remove duplicated column heading
new.data <- new.data[ -c(10) ]

##New patch size analysis script##
  ptch.list <- vector(mode = "list", length = nyear)
  for (i in 1:nyear) {
    #i <- 3
    data <- new.data %>%
      filter(year == i)

    data.2 <- data[!duplicated(data$id), ]

    data.2 <- data.2 %>%
      filter(flow > 0)

    new.id <- data.2$id

    #We can subset shapefiles using the following:
    std.shp <- subset(shp.new, StdID %in% new.id)
    #plot(std.shp)
    #Convert shapefile into raster
    site.vect <- vect(std.shp)
    #plot(site.vect)
    raster_template <- rast(ext(site.vect), resolution = 10,
                            crs = "+proj=utm +zone=16 +datum=NAD83 +units=m +no_defs")
    site.rast <- rasterize(site.vect, raster_template)
    ##Start lsm tests##
    #lsm.test <- lsm_p_enn(site.rast)
    lsm.test <- calculate_lsm(site.rast, level = level)
    #lsm.test
    write_csv(x = lsm.test,
              file = paste0("./lsm_sum_", i, ".csv"))
    ##End lsm tests##

    #plot(site.vect)
    #site.vect
    #site.rast
    #plot(site.rast)
    if(mode == "terrestrial"){
      test <- patches(site.rast, allowGaps = F)
      #test <- na.omit(test)
      ptch.size <- cellSize(test, unit="ha") |> zonal(test, sum)
      ptch.size$area <- round(ptch.size$area, digits = 2)
      ptch.size$year <- i
      ptch.list[[i]] <- ptch.size
    } else if(mode == "avian"){
      #Get patch locations
      test <- patches(site.rast, allowGaps = F)
      #Convert patch Spatraster to Spatvector
      vect.test <- as.polygons(test)
      #plot(vect.test.2)
      #Find patches which are close enough to move between (default = 500)
      nby.test <- nearby(vect.test, distance = dist)
      #nby.test <- adjacent(site.shp, pairs = T)
      #Convert to data frame
      nby.test <- data.frame(nby.test)
      #New nested loop to replace patch values for larger patches
      #This is based on the patches that are accessible
      if(nrow(nby.test) < 1){
        test <- test
      }else if(nrow(nby.test) > 0) {
      for(k in 1:nrow(nby.test)){
        test <- classify(test, cbind(nby.test[k,1], nby.test[k,2]))
      }
      }
      ptch.size <- cellSize(test, unit="ha") |> zonal(test, sum)
      ptch.size$area <- round(ptch.size$area, digits = 2)
      ptch.size$year <- i
      ptch.list[[i]] <- ptch.size
    }

    #Plot patch maps
    rcw.shp <- ggplot() +
      geom_spatraster(data = test, aes(fill = patches)) +
      scale_fill_viridis_c(option = "viridis",
                           name = "Patch ID", na.value = "white") +
      coord_sf(crs = "+proj=utm +zone=16 +datum=NAD83 +units=m +no_defs") +
      ggtitle(paste0("Patch map - year ", i)) +
      xlab("X") +
      ylab("Y") +
      theme_classic() +
      theme(strip.background = element_blank())

    rcw.shp

    ggsave(file = paste0("./Patch_map_", i, ".png"),
           width = 200, height = 120, dpi = 600, units = "mm")
  }
  patch.data <- bind_rows(ptch.list, .id = "year")
  write_csv(x = patch.data,
            file = "./Patch_data.csv")

  ptch.list.2 <- vector(mode = "list", length = nyear)
  for (j in 1:nyear) {
    year <- j
    year <- as.numeric(year)
    data.yr <- patch.data %>%
      filter(year == j)
    min.patch <- as.numeric(min(data.yr$area))
    max.patch <- as.numeric(max(data.yr$area))
    mean.patch <- as.numeric(round(mean(data.yr$area), digits = 2))
    sum.patch <- as.numeric(max(data.yr$patches))
    tot.patch <- as.numeric(sum(data.yr$area))
    ptch.sum <- data.frame(cbind(year, sum.patch, min.patch, max.patch, mean.patch,
                                 tot.patch))
    ptch.list.2[[j]] <- ptch.sum
  }

  patch.sum <- bind_rows(ptch.list.2, .id = "year")

  order <- factor(patch.sum$year, level = c(1:35))

  #Plot patch summary figures
  max.ptch <- ggplot() +
    geom_line(data = patch.sum, aes(x = order, y = max.patch),
              group = year) +
    #scale_color_viridis_c() +
    ggtitle(paste0("Max patch size")) +
    xlab("Year") +
    ylab("Max patch size (ha)") +
    theme_classic() +
    theme(strip.background = element_blank())

  max.ptch

  ggsave(file = paste0("./Max_patch.png"),
         width = 200, height = 120, dpi = 600, units = "mm")

  #Plot patch summary figures
  min.ptch <- ggplot() +
    geom_line(data = patch.sum, aes(x = order, y = min.patch),
              group = year) +
    ggtitle(paste0("Min patch size")) +
    xlab("Year") +
    ylab("Min patch size (ha)") +
    theme_classic() +
    theme(strip.background = element_blank())

  min.ptch

  ggsave(file = paste0("./Min_patch.png"),
         width = 200, height = 120, dpi = 600, units = "mm")

  #Plot patch summary figures
  mean.ptch <- ggplot() +
    geom_line(data = patch.sum, aes(x = order, y = mean.patch),
              group = year) +
    ggtitle(paste0("Mean patch size ")) +
    xlab("Year") +
    ylab("Mean patch size (ha)") +
    theme_classic() +
    theme(strip.background = element_blank())

  mean.ptch

  ggsave(file = paste0("./Mean_patch.png"),
         width = 200, height = 120, dpi = 600, units = "mm")

  #Plot patch summary figures
  tot.ptch <- ggplot() +
    geom_line(data = patch.sum, aes(x = order, y = tot.patch),
              group = year) +
    ggtitle(paste0("Total patch area")) +
    xlab("Year") +
    ylab("Total patch area (ha)") +
    theme_classic() +
    theme(strip.background = element_blank())

  tot.ptch

  ggsave(file = paste0("./Total_patch.png"),
         width = 200, height = 120, dpi = 600, units = "mm")

  #Plot patch summary figures
  sum.ptch <- ggplot() +
    geom_line(data = patch.sum, aes(x = order, y = sum.patch),
              group = year) +
    ggtitle(paste0("Total number of patches")) +
    xlab("Year") +
    ylab("Total number of patches") +
    theme_classic() +
    theme(strip.background = element_blank())

  sum.ptch

  ggsave(file = paste0("./Tot_num_patch.png"),
         width = 200, height = 120, dpi = 600, units = "mm")

  colnames(patch.sum) <- c("Year", "Number of patches", "Min. patch size",
                           "Max. patch size", "Mean patch size", "Total area")
  write_csv(x = patch.sum,
            file = "./Patch_data_summary.csv")


  return(patch.sum)
}


