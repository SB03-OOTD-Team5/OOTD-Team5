import LoginUpperSectionSvg from "@/assets/illust_logos/login upper section.svg";

export default function WeatherIconsHeader() {
  return (
    <div className="flex items-center justify-center mb-[-40px] relative z-[2]">
      <img 
        src={LoginUpperSectionSvg} 
        alt="날씨 아이콘들" 
        className="w-[517px] h-[97px] object-contain" 
      />
    </div>
  );
}