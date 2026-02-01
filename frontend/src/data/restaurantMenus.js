const defaultMenuCategories = [
  {
    id: 'main',
    name: '메인 메뉴',
    items: [],
  },
  {
    id: 'sub',
    name: '사이드 메뉴',
    items: [],
  },
  {
    id: 'other',
    name: '음료/기타',
    items: [],
  },
];

const restaurantMenuCatalog = {
  1: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '도가니수육',
          price: '38,000원',
          description: '쫀득한 도가니를 부드럽게 삶아낸 수육 메뉴.',
        },
        {
          id: 'main-2',
          name: '도가니탕',
          price: '19,000원',
          description: '도가니를 오래 끓여 쫀득한 식감과 깊은 국물 맛이 특징.',
        },
        {
          id: 'main-3',
          name: '설렁탕',
          price: '12,000원',
          description: '우려낸 사골 육수에 부드러운 양지와 소면을 곁들인 담백한 국물.',
        },
        {
          id: 'main-4',
          name: '양지수육',
          price: '30,000원',
          description: '삶은 양지를 얇게 썰어 담백하게 즐기는 수육.',
        },
        {
          id: 'main-5',
          name: '특설렁탕',
          price: '15,000원',
          description: '고기 양을 넉넉히 담아 진한 사골 맛을 즐길 수 있는 설렁탕.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '떡사리',
          price: '3,000원',
          description: '쫄깃한 떡을 추가해 국물과 함께 즐기는 사리.',
        },
        {
          id: 'sub-2',
          name: '소면사리',
          price: '3,000원',
          description: '사골국물에 말아 먹기 좋은 소면 사리.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '속을 꽉 채운 큼직한 만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '생맥주',
          price: '5,000원',
          description: '시원하게 즐기는 생맥주 1잔.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '국물요리와 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  2: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '16,000원',
          description: '진하게 우려낸 갈비탕에 당면을 곁들인 보양 국물.',
        },
        {
          id: 'main-2',
          name: '물냉면',
          price: '12,000원',
          description: '동치미와 육수를 섞어 시원하게 즐기는 냉면.',
        },
        {
          id: 'main-3',
          name: '비빔냉면',
          price: '12,000원',
          description: '매콤달콤한 양념에 비벼 먹는 냉면.',
        },
        {
          id: 'main-4',
          name: '수육',
          price: '30,000원',
          description: '부드럽게 삶아낸 돼지고기 수육을 넉넉히 제공.',
        },
        {
          id: 'main-5',
          name: '회냉면',
          price: '14,000원',
          description: '새콤한 양념과 회를 올려 입맛 돋우는 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '녹두전',
          price: '9,000원',
          description: '고소하게 부친 녹두전 한 장.',
        },
        {
          id: 'sub-2',
          name: '비빔만두',
          price: '8,000원',
          description: '만두를 매콤한 양념에 버무린 인기 메뉴.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '냉면과 같이 먹기 좋은 왕만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기기 좋은 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '사이다',
          price: '3,000원',
          description: '매콤한 메뉴와 어울리는 탄산음료.',
        },
      ],
    },
  ],
  3: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  4: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '고등어구이',
          price: '12,000원',
          description: '노릇하게 구운 고등어구이.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '9,000원',
          description: '구수하게 끓여낸 된장찌개.',
        },
        {
          id: 'main-3',
          name: '뚝배기불고기',
          price: '13,000원',
          description: '달짝지근한 불고기를 뚝배기에 끓여낸 메뉴.',
        },
        {
          id: 'main-4',
          name: '비빔밥',
          price: '11,000원',
          description: '신선한 나물과 고추장을 비벼 먹는 비빔밥.',
        },
        {
          id: 'main-5',
          name: '제육볶음',
          price: '12,000원',
          description: '매콤하게 볶아낸 돼지고기 제육볶음.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤하게 무친 도토리묵무침.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '한식과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  5: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돼지국밥',
          price: '10,000원',
          description: '진한 육수에 돼지고기를 듬뿍 담은 국밥.',
        },
        {
          id: 'main-2',
          name: '모둠순대',
          price: '22,000원',
          description: '순대와 내장, 머릿고기를 함께 즐기는 모둠.',
        },
        {
          id: 'main-3',
          name: '수육',
          price: '28,000원',
          description: '부드럽게 삶아낸 수육 한 접시.',
        },
        {
          id: 'main-4',
          name: '순대국밥',
          price: '11,000원',
          description: '순대와 내장을 넣은 국밥.',
        },
        {
          id: 'main-5',
          name: '평양냉면',
          price: '13,000원',
          description: '메밀향과 육향이 어우러진 담백한 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥 추가',
          price: '1,000원',
          description: '국밥과 함께 먹는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '야채전',
          price: '7,000원',
          description: '바삭하게 부친 야채전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '국밥과 궁합 좋은 소주 1병.',
        },
      ],
    },
  ],
  6: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  7: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가케우동',
          price: '9,500원',
          description: '따뜻한 가쓰오 육수에 즐기는 기본 우동.',
        },
        {
          id: 'main-2',
          name: '모둠튀김우동',
          price: '13,000원',
          description: '새우와 야채튀김을 올린 든든한 우동.',
        },
        {
          id: 'main-3',
          name: '붓카케우동',
          price: '10,000원',
          description: '차갑게 즐기는 쫄깃한 자가제면 우동.',
        },
        {
          id: 'main-4',
          name: '자루소바',
          price: '10,000원',
          description: '시원한 소바와 찍먹 쯔유를 함께 제공.',
        },
        {
          id: 'main-5',
          name: '카레우동',
          price: '11,000원',
          description: '진한 카레 소스에 우동면을 더한 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '7,000원',
          description: '간장 베이스로 양념한 가라아게 한 접시.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '유부초밥 4개',
          price: '4,000원',
          description: '달콤한 유부에 밥을 채운 유부초밥 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사이다',
          price: '3,000원',
          description: '깔끔하게 마시기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '5,000원',
          description: '튀김과 잘 어울리는 생맥주 1잔.',
        },
      ],
    },
  ],
  8: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  9: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  10: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  11: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  12: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가라아게동',
          price: '12,000원',
          description: '가라아게를 듬뿍 올린 치킨 덮밥.',
        },
        {
          id: 'main-2',
          name: '가츠동',
          price: '12,000원',
          description: '바삭한 돈가스를 달걀로 덮어 밥 위에 올린 덮밥.',
        },
        {
          id: 'main-3',
          name: '규동',
          price: '12,000원',
          description: '달콤짭짤한 소고기볶음을 올린 규동.',
        },
        {
          id: 'main-4',
          name: '사케동',
          price: '16,000원',
          description: '연어를 올려 즐기는 연어덮밥.',
        },
        {
          id: 'main-5',
          name: '에비텐동',
          price: '14,000원',
          description: '새우튀김을 올린 덴푸라 덮밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '교자만두 5개',
          price: '5,000원',
          description: '바삭하게 구운 교자만두 5개.',
        },
        {
          id: 'sub-2',
          name: '미니우동',
          price: '4,000원',
          description: '덮밥과 곁들이기 좋은 미니 우동.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '탄산음료',
          price: '3,000원',
          description: '가볍게 곁들이는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원하게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  13: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '고등어구이',
          price: '12,000원',
          description: '노릇하게 구운 고등어구이.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '9,000원',
          description: '구수하게 끓여낸 된장찌개.',
        },
        {
          id: 'main-3',
          name: '뚝배기불고기',
          price: '13,000원',
          description: '달짝지근한 불고기를 뚝배기에 끓여낸 메뉴.',
        },
        {
          id: 'main-4',
          name: '비빔밥',
          price: '11,000원',
          description: '신선한 나물과 고추장을 비벼 먹는 비빔밥.',
        },
        {
          id: 'main-5',
          name: '제육볶음',
          price: '12,000원',
          description: '매콤하게 볶아낸 돼지고기 제육볶음.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤하게 무친 도토리묵무침.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '한식과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  14: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '목살 샤브샤브',
          price: '15,000원',
          description: '담백한 돼지 목살로 즐기는 샤브샤브.',
        },
        {
          id: 'main-2',
          name: '백합 샤브샤브',
          price: '20,000원',
          description: '산지 직송 백합을 넣어 시원하게 즐기는 샤브샤브.',
        },
        {
          id: 'main-3',
          name: '버섯 샤브샤브',
          price: '15,000원',
          description: '다양한 버섯을 듬뿍 넣은 샤브샤브.',
        },
        {
          id: 'main-4',
          name: '소고기 샤브샤브',
          price: '16,000원',
          description: '얇게 썬 소고기를 육수에 살짝 익혀 즐기는 샤브샤브.',
        },
        {
          id: 'main-5',
          name: '해물 샤브샤브',
          price: '18,000원',
          description: '새우와 조개 등 해물을 더한 샤브샤브.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '만두사리',
          price: '4,000원',
          description: '쫄깃한 만두를 추가해 즐기는 사리.',
        },
        {
          id: 'sub-2',
          name: '죽',
          price: '3,000원',
          description: '남은 육수로 끓여 고소한 맛을 낸 죽.',
        },
        {
          id: 'sub-3',
          name: '칼국수사리',
          price: '3,000원',
          description: '샤브 육수에 넣어 마무리하는 칼국수사리.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '샤브샤브와 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔하게 즐기는 소주 1병.',
        },
      ],
    },
  ],
  15: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈코츠라멘',
          price: '11,000원',
          description: '진하게 우린 돼지뼈 육수에 차슈를 올린 라멘.',
        },
        {
          id: 'main-2',
          name: '미소라멘',
          price: '11,000원',
          description: '된장 풍미가 살아있는 미소 라멘.',
        },
        {
          id: 'main-3',
          name: '쇼유라멘',
          price: '10,500원',
          description: '간장 베이스 국물로 깔끔하게 즐기는 라멘.',
        },
        {
          id: 'main-4',
          name: '차슈덮밥',
          price: '10,000원',
          description: '달짝지근한 차슈와 밥을 함께 즐기는 덮밥.',
        },
        {
          id: 'main-5',
          name: '츠케멘',
          price: '12,000원',
          description: '진한 찍먹 소스에 면을 담가 먹는 라멘.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게 5조각',
          price: '7,000원',
          description: '촉촉하게 튀겨낸 가라아게 5조각.',
        },
        {
          id: 'sub-2',
          name: '교자만두 5개',
          price: '5,000원',
          description: '바삭하게 구운 교자만두 5개.',
        },
        {
          id: 'sub-3',
          name: '아지타마고',
          price: '3,000원',
          description: '반숙으로 간을 배인 라멘 토핑 계란 1개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '탄산음료',
          price: '3,000원',
          description: '가볍게 곁들이기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '라멘과 잘 어울리는 하이볼 1잔.',
        },
      ],
    },
  ],
  16: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '제철 회를 다양하게 구성한 사시미 모둠.',
        },
        {
          id: 'main-2',
          name: '연어아보카도동',
          price: '18,000원',
          description: '연어와 아보카도를 함께 즐기는 덮밥.',
        },
        {
          id: 'main-3',
          name: '우니동',
          price: '32,000원',
          description: '고소한 성게알(우니)을 올린 덮밥.',
        },
        {
          id: 'main-4',
          name: '카이센동',
          price: '17,000원',
          description: '신선한 해산물을 듬뿍 올린 일본식 해산물덮밥.',
        },
        {
          id: 'main-5',
          name: '혼마구로동',
          price: '24,000원',
          description: '진한 풍미의 참다랑어를 올린 덮밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '덮밥과 곁들이기 좋은 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '사케',
          price: '12,000원',
          description: '해산물과 잘 어울리는 사케 1병(소)',
        },
      ],
    },
  ],
  17: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  18: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '눈다랑어 등살',
          price: '48,000원',
          description: '담백한 식감의 눈다랑어 등살.',
        },
        {
          id: 'main-2',
          name: '참다랑어 뱃살',
          price: '65,000원',
          description: '고소한 지방이 풍부한 참다랑어 뱃살.',
        },
        {
          id: 'main-3',
          name: '참치모둠회(소)',
          price: '55,000원',
          description: '부위별로 골고루 즐기는 참치 모둠회(소).',
        },
        {
          id: 'main-4',
          name: '참치초밥 10피스',
          price: '22,000원',
          description: '참치 중심으로 구성한 초밥 10피스.',
        },
        {
          id: 'main-5',
          name: '참치타다키',
          price: '25,000원',
          description: '겉만 살짝 구워 향을 살린 참치 타다키.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '8,000원',
          description: '겉은 바삭, 속은 촉촉한 치킨 가라아게.',
        },
        {
          id: 'sub-2',
          name: '계란말이',
          price: '7,000원',
          description: '폭신하게 말아낸 두툼한 계란말이.',
        },
        {
          id: 'sub-3',
          name: '명란구이',
          price: '9,000원',
          description: '짭짤고소한 명란을 구워낸 안주.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '14,000원',
          description: '참치와 궁합 좋은 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '가볍게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  19: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  20: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  21: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  22: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  23: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  24: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '두부김치',
          price: '18,000원',
          description: '따뜻한 두부와 볶은 김치를 함께 제공.',
        },
        {
          id: 'main-2',
          name: '두부전골',
          price: '22,000원',
          description: '두부와 채소를 듬뿍 넣어 끓인 전골.',
        },
        {
          id: 'main-3',
          name: '들깨두부탕',
          price: '12,000원',
          description: '들깨가루로 고소하게 끓여낸 두부탕.',
        },
        {
          id: 'main-4',
          name: '순두부찌개',
          price: '10,000원',
          description: '부드러운 순두부와 얼큰한 국물의 찌개.',
        },
        {
          id: 'main-5',
          name: '해물순두부찌개',
          price: '12,000원',
          description: '해물을 더해 시원한 맛을 살린 순두부찌개.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자전',
          price: '9,000원',
          description: '감자를 갈아 고소하게 부친 감자전.',
        },
        {
          id: 'sub-2',
          name: '메밀전병',
          price: '8,000원',
          description: '고소한 메밀전병 2개.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 바삭하게 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  25: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '전복해물탕',
          price: '58,000원',
          description: '전복을 더해 깊은 맛을 낸 해물탕.',
        },
        {
          id: 'main-2',
          name: '해물모둠',
          price: '45,000원',
          description: '제철 해산물을 다양하게 즐기는 해물모둠.',
        },
        {
          id: 'main-3',
          name: '해물찜',
          price: '52,000원',
          description: '매콤한 양념에 해산물을 쪄낸 해물찜.',
        },
        {
          id: 'main-4',
          name: '해물탕',
          price: '48,000원',
          description: '전복, 새우, 조개를 넣어 시원하게 끓인 해물탕.',
        },
        {
          id: 'main-5',
          name: '회 모둠(소)',
          price: '38,000원',
          description: '신선한 회를 모둠으로 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살볶음밥',
          price: '9,000원',
          description: '고소한 게살을 넣어 볶은 볶음밥.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '해물라면',
          price: '8,000원',
          description: '해산물 육수로 끓인 얼큰한 해물라면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '해산물과 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  26: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  27: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '감바스 알 아히요',
          price: '17,000원',
          description: '마늘 오일에 새우를 익힌 스페인식 타파스.',
        },
        {
          id: 'main-2',
          name: '문어구이',
          price: '19,000원',
          description: '부드럽게 익힌 문어를 그릴에 구워낸 메뉴.',
        },
        {
          id: 'main-3',
          name: '오징어튀김',
          price: '15,000원',
          description: '바삭한 오징어 튀김과 레몬을 곁들임.',
        },
        {
          id: 'main-4',
          name: '하몽크로켓',
          price: '14,000원',
          description: '하몽을 넣어 고소하게 튀긴 크로켓.',
        },
        {
          id: 'main-5',
          name: '해산물빠에야',
          price: '22,000원',
          description: '해산물과 사프란 향이 어우러진 빠에야.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '치즈플래터',
          price: '12,000원',
          description: '치즈를 간단히 즐길 수 있는 플래터.',
        },
        {
          id: 'sub-2',
          name: '토마토샐러드',
          price: '9,000원',
          description: '신선한 토마토와 올리브오일 샐러드.',
        },
        {
          id: 'sub-3',
          name: '트러플감자튀김',
          price: '8,000원',
          description: '트러플 향을 더한 감자튀김.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '상그리아',
          price: '12,000원',
          description: '과일을 넣어 만든 상그리아 1잔.',
        },
        {
          id: 'other-2',
          name: '하우스와인',
          price: '12,000원',
          description: '글라스로 즐기는 하우스와인 1잔.',
        },
      ],
    },
  ],
  28: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '장어구이(소금)',
          price: '29,000원',
          description: '참숯에 구운 소금 장어구이.',
        },
        {
          id: 'main-2',
          name: '장어구이(양념)',
          price: '29,000원',
          description: '달콤한 양념 장어구이.',
        },
        {
          id: 'main-3',
          name: '장어덮밥',
          price: '22,000원',
          description: '구운 장어를 밥 위에 올린 덮밥.',
        },
        {
          id: 'main-4',
          name: '장어탕',
          price: '14,000원',
          description: '구수하게 끓여낸 장어탕.',
        },
        {
          id: 'main-5',
          name: '히쯔마부시',
          price: '26,000원',
          description: '나고야식 장어덮밥으로 3가지 방식으로 즐기는 히쯔마부시.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥',
          price: '1,000원',
          description: '구이와 곁들이는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '4,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '장어구이와 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  29: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가케우동',
          price: '9,500원',
          description: '따뜻한 가쓰오 육수에 즐기는 기본 우동.',
        },
        {
          id: 'main-2',
          name: '모둠튀김우동',
          price: '13,000원',
          description: '새우와 야채튀김을 올린 든든한 우동.',
        },
        {
          id: 'main-3',
          name: '붓카케우동',
          price: '10,000원',
          description: '차갑게 즐기는 쫄깃한 자가제면 우동.',
        },
        {
          id: 'main-4',
          name: '자루소바',
          price: '10,000원',
          description: '시원한 소바와 찍먹 쯔유를 함께 제공.',
        },
        {
          id: 'main-5',
          name: '카레우동',
          price: '11,000원',
          description: '진한 카레 소스에 우동면을 더한 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '7,000원',
          description: '간장 베이스로 양념한 가라아게 한 접시.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '유부초밥 4개',
          price: '4,000원',
          description: '달콤한 유부에 밥을 채운 유부초밥 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사이다',
          price: '3,000원',
          description: '깔끔하게 마시기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '5,000원',
          description: '튀김과 잘 어울리는 생맥주 1잔.',
        },
      ],
    },
  ],
  30: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '디너뷔페(성인)',
          price: '42,000원',
          description: '저녁시간 이용 가능한 성인 뷔페 이용권.',
        },
        {
          id: 'main-2',
          name: '디너뷔페(초등학생)',
          price: '23,000원',
          description: '초등학생 디너 뷔페 이용권.',
        },
        {
          id: 'main-3',
          name: '런치뷔페(성인)',
          price: '32,000원',
          description: '점심시간 이용 가능한 성인 뷔페 이용권.',
        },
        {
          id: 'main-4',
          name: '런치뷔페(초등학생)',
          price: '18,000원',
          description: '초등학생 런치 뷔페 이용권.',
        },
        {
          id: 'main-5',
          name: '주말뷔페(성인)',
          price: '48,000원',
          description: '주말/공휴일 성인 뷔페 이용권.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '디저트 플래터',
          price: '9,000원',
          description: '케이크와 과일로 구성한 디저트 플래터.',
        },
        {
          id: 'sub-2',
          name: '무제한 생맥주',
          price: '12,000원',
          description: '뷔페 이용 시 추가 가능한 무제한 생맥주 옵션.',
        },
        {
          id: 'sub-3',
          name: '무제한 와인',
          price: '15,000원',
          description: '뷔페 이용 시 추가 가능한 무제한 와인 옵션.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '아메리카노',
          price: '4,500원',
          description: '식사 후 깔끔하게 즐기는 아메리카노.',
        },
        {
          id: 'other-2',
          name: '탄산음료',
          price: '3,000원',
          description: '가볍게 곁들이는 탄산음료.',
        },
      ],
    },
  ],
  31: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  32: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '양갈비',
          price: '28,000원',
          description: '두툼한 양갈비를 숯불에 구워 즐기는 메뉴.',
        },
        {
          id: 'main-2',
          name: '양갈비살',
          price: '25,000원',
          description: '먹기 좋게 손질한 양갈비살 구이.',
        },
        {
          id: 'main-3',
          name: '양고기볶음밥',
          price: '12,000원',
          description: '양고기와 채소를 넣어 볶은 고소한 볶음밥.',
        },
        {
          id: 'main-4',
          name: '양꼬치 10꼬치',
          price: '19,000원',
          description: '향신료를 더해 구운 양꼬치 10꼬치.',
        },
        {
          id: 'main-5',
          name: '양등심',
          price: '26,000원',
          description: '담백하고 부드러운 양등심 구이.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '꿔바로우',
          price: '12,000원',
          description: '바삭한 튀김옷에 새콤한 소스를 입힌 꿔바로우.',
        },
        {
          id: 'sub-2',
          name: '마파두부',
          price: '11,000원',
          description: '얼큰한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'sub-3',
          name: '온면',
          price: '9,000원',
          description: '따뜻한 국물에 면을 말아낸 중국식 온면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '칭따오',
          price: '6,500원',
          description: '양고기와 잘 어울리는 칭따오 맥주 1병.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '깔끔하게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  33: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  34: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  35: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '고등어구이',
          price: '12,000원',
          description: '노릇하게 구운 고등어구이.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '9,000원',
          description: '구수하게 끓여낸 된장찌개.',
        },
        {
          id: 'main-3',
          name: '뚝배기불고기',
          price: '13,000원',
          description: '달짝지근한 불고기를 뚝배기에 끓여낸 메뉴.',
        },
        {
          id: 'main-4',
          name: '비빔밥',
          price: '11,000원',
          description: '신선한 나물과 고추장을 비벼 먹는 비빔밥.',
        },
        {
          id: 'main-5',
          name: '제육볶음',
          price: '12,000원',
          description: '매콤하게 볶아낸 돼지고기 제육볶음.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤하게 무친 도토리묵무침.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '한식과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  36: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '민물장어 한마리',
          price: '52,000원',
          description: '두툼한 민물장어 한마리를 통째로 구워 제공.',
        },
        {
          id: 'main-2',
          name: '장어구이(소금)',
          price: '29,000원',
          description: '참숯에 구워 장어 본연의 맛을 살린 소금구이.',
        },
        {
          id: 'main-3',
          name: '장어구이(양념)',
          price: '29,000원',
          description: '달콤한 양념을 발라 구운 장어구이.',
        },
        {
          id: 'main-4',
          name: '장어덮밥',
          price: '22,000원',
          description: '구운 장어를 밥 위에 올린 든든한 덮밥.',
        },
        {
          id: 'main-5',
          name: '장어탕',
          price: '14,000원',
          description: '구수하게 끓여낸 장어탕.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥',
          price: '1,000원',
          description: '구이와 곁들이는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '4,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '장어구이와 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  37: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '16,000원',
          description: '진하게 우려낸 갈비탕에 당면을 곁들인 보양 국물.',
        },
        {
          id: 'main-2',
          name: '물냉면',
          price: '12,000원',
          description: '동치미와 육수를 섞어 시원하게 즐기는 냉면.',
        },
        {
          id: 'main-3',
          name: '비빔냉면',
          price: '12,000원',
          description: '매콤달콤한 양념에 비벼 먹는 냉면.',
        },
        {
          id: 'main-4',
          name: '수육',
          price: '30,000원',
          description: '부드럽게 삶아낸 돼지고기 수육을 넉넉히 제공.',
        },
        {
          id: 'main-5',
          name: '회냉면',
          price: '14,000원',
          description: '새콤한 양념과 회를 올려 입맛 돋우는 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '녹두전',
          price: '9,000원',
          description: '고소하게 부친 녹두전 한 장.',
        },
        {
          id: 'sub-2',
          name: '비빔만두',
          price: '8,000원',
          description: '만두를 매콤한 양념에 버무린 인기 메뉴.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '냉면과 같이 먹기 좋은 왕만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기기 좋은 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '사이다',
          price: '3,000원',
          description: '매콤한 메뉴와 어울리는 탄산음료.',
        },
      ],
    },
  ],
  38: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  39: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  40: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '눈다랑어 등살',
          price: '48,000원',
          description: '담백한 식감의 눈다랑어 등살.',
        },
        {
          id: 'main-2',
          name: '참다랑어 뱃살',
          price: '65,000원',
          description: '고소한 지방이 풍부한 참다랑어 뱃살.',
        },
        {
          id: 'main-3',
          name: '참치모둠회(소)',
          price: '55,000원',
          description: '부위별로 골고루 즐기는 참치 모둠회(소).',
        },
        {
          id: 'main-4',
          name: '참치초밥 10피스',
          price: '22,000원',
          description: '참치 중심으로 구성한 초밥 10피스.',
        },
        {
          id: 'main-5',
          name: '참치타다키',
          price: '25,000원',
          description: '겉만 살짝 구워 향을 살린 참치 타다키.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '8,000원',
          description: '겉은 바삭, 속은 촉촉한 치킨 가라아게.',
        },
        {
          id: 'sub-2',
          name: '계란말이',
          price: '7,000원',
          description: '폭신하게 말아낸 두툼한 계란말이.',
        },
        {
          id: 'sub-3',
          name: '명란구이',
          price: '9,000원',
          description: '짭짤고소한 명란을 구워낸 안주.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '14,000원',
          description: '참치와 궁합 좋은 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '가볍게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  41: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  42: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '군만두',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'main-2',
          name: '볶음밥',
          price: '11,000원',
          description: '고슬고슬하게 볶아낸 중식 볶음밥.',
        },
        {
          id: 'main-3',
          name: '수타짜장',
          price: '10,000원',
          description: '수타면에 진한 짜장소스를 곁들인 메뉴.',
        },
        {
          id: 'main-4',
          name: '수타짬뽕',
          price: '12,000원',
          description: '수타면과 해물로 깊고 얼큰한 짬뽕.',
        },
        {
          id: 'main-5',
          name: '즉석탕수육',
          price: '22,000원',
          description: '주문 즉시 튀겨낸 바삭한 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드러운 게살스프.',
        },
        {
          id: 'sub-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 소스를 올린 유린기.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 1병.',
        },
        {
          id: 'other-2',
          name: '콜라',
          price: '3,000원',
          description: '깔끔한 탄산음료.',
        },
      ],
    },
  ],
  43: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '곱창전골',
          price: '32,000원',
          description: '곱창과 채소를 넣어 얼큰하게 끓인 전골.',
        },
        {
          id: 'main-2',
          name: '낙곱새',
          price: '28,000원',
          description: '낙지·곱창·새우를 매콤하게 끓여낸 메뉴.',
        },
        {
          id: 'main-3',
          name: '낙지볶음',
          price: '18,000원',
          description: '매콤하게 볶아낸 낙지볶음.',
        },
        {
          id: 'main-4',
          name: '대창덮밥',
          price: '14,000원',
          description: '고소한 대창을 밥 위에 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '볶음밥',
          price: '3,000원',
          description: '남은 양념에 볶아 먹는 볶음밥(1인).',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '우동사리',
          price: '3,000원',
          description: '매콤한 양념에 넣어 즐기는 우동사리.',
        },
        {
          id: 'sub-3',
          name: '치즈추가',
          price: '3,000원',
          description: '치즈를 더해 풍미를 살림.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '매콤한 메뉴와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  44: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  45: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  46: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '전복해물탕',
          price: '58,000원',
          description: '전복을 더해 깊은 맛을 낸 해물탕.',
        },
        {
          id: 'main-2',
          name: '해물모둠',
          price: '45,000원',
          description: '제철 해산물을 다양하게 즐기는 해물모둠.',
        },
        {
          id: 'main-3',
          name: '해물찜',
          price: '52,000원',
          description: '매콤한 양념에 해산물을 쪄낸 해물찜.',
        },
        {
          id: 'main-4',
          name: '해물탕',
          price: '48,000원',
          description: '전복, 새우, 조개를 넣어 시원하게 끓인 해물탕.',
        },
        {
          id: 'main-5',
          name: '회 모둠(소)',
          price: '38,000원',
          description: '신선한 회를 모둠으로 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살볶음밥',
          price: '9,000원',
          description: '고소한 게살을 넣어 볶은 볶음밥.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '해물라면',
          price: '8,000원',
          description: '해산물 육수로 끓인 얼큰한 해물라면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '해산물과 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  47: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  48: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  49: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '목살 샤브샤브',
          price: '15,000원',
          description: '담백한 돼지 목살로 즐기는 샤브샤브.',
        },
        {
          id: 'main-2',
          name: '버섯 샤브샤브',
          price: '15,000원',
          description: '다양한 버섯을 듬뿍 넣은 샤브샤브.',
        },
        {
          id: 'main-3',
          name: '소고기 샤브샤브',
          price: '16,000원',
          description: '얇게 썬 소고기를 육수에 살짝 익혀 즐기는 샤브샤브.',
        },
        {
          id: 'main-4',
          name: '야채 샤브샤브',
          price: '14,000원',
          description: '신선한 채소 위주로 가볍게 즐기는 샤브샤브.',
        },
        {
          id: 'main-5',
          name: '해물 샤브샤브',
          price: '18,000원',
          description: '새우와 조개 등 해물을 더한 샤브샤브.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '만두사리',
          price: '4,000원',
          description: '쫄깃한 만두를 추가해 즐기는 사리.',
        },
        {
          id: 'sub-2',
          name: '죽',
          price: '3,000원',
          description: '남은 육수로 끓여 고소한 맛을 낸 죽.',
        },
        {
          id: 'sub-3',
          name: '칼국수사리',
          price: '3,000원',
          description: '샤브 육수에 넣어 마무리하는 칼국수사리.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '샤브샤브와 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔하게 즐기는 소주 1병.',
        },
      ],
    },
  ],
  50: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '감자탕',
          price: '28,000원',
          description: '뼈와 우거지를 푹 끓여 얼큰하게 즐기는 감자탕.',
        },
        {
          id: 'main-2',
          name: '등뼈찜',
          price: '32,000원',
          description: '매콤한 양념에 조린 등뼈찜.',
        },
        {
          id: 'main-3',
          name: '묵은지감자탕',
          price: '30,000원',
          description: '묵은지의 깊은 맛을 더한 감자탕.',
        },
        {
          id: 'main-4',
          name: '뼈해장국',
          price: '11,000원',
          description: '진한 뼈국물에 얼큰한 양념을 더한 해장국.',
        },
        {
          id: 'main-5',
          name: '우거지해장국',
          price: '10,000원',
          description: '우거지를 듬뿍 넣어 구수하게 끓인 해장국.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '라면사리',
          price: '3,000원',
          description: '얼큰한 국물에 넣어 먹는 라면사리.',
        },
        {
          id: 'sub-2',
          name: '볶음밥',
          price: '3,000원',
          description: '남은 국물과 함께 볶아 먹는 볶음밥.',
        },
        {
          id: 'sub-3',
          name: '수제비사리',
          price: '3,000원',
          description: '쫄깃한 수제비를 추가해 국물과 함께.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '얼큰한 메뉴와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  51: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '멸치국수',
          price: '10,000원',
          description: '멸치육수로 담백하게 만든 국수.',
        },
        {
          id: 'main-2',
          name: '멸치쌈밥',
          price: '12,000원',
          description: '멸치볶음과 쌈채소로 즐기는 쌈밥.',
        },
        {
          id: 'main-3',
          name: '멸치회무침',
          price: '18,000원',
          description: '새콤하게 무친 멸치회무침.',
        },
        {
          id: 'main-4',
          name: '생선구이',
          price: '13,000원',
          description: '노릇하게 구운 제철 생선구이.',
        },
        {
          id: 'main-5',
          name: '우렁이된장찌개',
          price: '11,000원',
          description: '우렁이를 넣어 구수하게 끓인 된장찌개.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥',
          price: '1,000원',
          description: '쌈밥과 곁들이는 공기밥.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '바삭하게 부친 해물파전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '쌈밥과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  52: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  53: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  54: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  55: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돼지국밥',
          price: '10,000원',
          description: '진하게 우린 육수에 돼지고기를 듬뿍 담은 국밥.',
        },
        {
          id: 'main-2',
          name: '모둠순대',
          price: '22,000원',
          description: '순대와 내장, 머릿고기를 함께 즐기는 모둠.',
        },
        {
          id: 'main-3',
          name: '수육',
          price: '28,000원',
          description: '부드럽게 삶아낸 돼지고기 수육 한 접시.',
        },
        {
          id: 'main-4',
          name: '수육국밥',
          price: '12,000원',
          description: '수육을 얹어 든든하게 즐기는 국밥.',
        },
        {
          id: 'main-5',
          name: '순대국밥',
          price: '11,000원',
          description: '순대와 내장을 넣어 구수한 맛이 나는 국밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥 추가',
          price: '1,000원',
          description: '국밥과 함께 먹는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '야채전',
          price: '7,000원',
          description: '바삭하게 부친 야채전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '국밥과 궁합 좋은 소주 1병.',
        },
      ],
    },
  ],
  56: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '고등어구이',
          price: '12,000원',
          description: '노릇하게 구운 고등어구이.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '9,000원',
          description: '구수하게 끓여낸 된장찌개.',
        },
        {
          id: 'main-3',
          name: '뚝배기불고기',
          price: '13,000원',
          description: '달짝지근한 불고기를 뚝배기에 끓여낸 메뉴.',
        },
        {
          id: 'main-4',
          name: '비빔밥',
          price: '11,000원',
          description: '신선한 나물과 고추장을 비벼 먹는 비빔밥.',
        },
        {
          id: 'main-5',
          name: '제육볶음',
          price: '12,000원',
          description: '매콤하게 볶아낸 돼지고기 제육볶음.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤하게 무친 도토리묵무침.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '한식과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  57: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  58: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가케우동',
          price: '9,500원',
          description: '따뜻한 가쓰오 육수에 즐기는 기본 우동.',
        },
        {
          id: 'main-2',
          name: '모둠튀김우동',
          price: '13,000원',
          description: '새우와 야채튀김을 올린 든든한 우동.',
        },
        {
          id: 'main-3',
          name: '붓카케우동',
          price: '10,000원',
          description: '차갑게 즐기는 쫄깃한 자가제면 우동.',
        },
        {
          id: 'main-4',
          name: '자루소바',
          price: '10,000원',
          description: '시원한 소바와 찍먹 쯔유를 함께 제공.',
        },
        {
          id: 'main-5',
          name: '카레우동',
          price: '11,000원',
          description: '진한 카레 소스에 우동면을 더한 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '7,000원',
          description: '간장 베이스로 양념한 가라아게 한 접시.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '유부초밥 4개',
          price: '4,000원',
          description: '달콤한 유부에 밥을 채운 유부초밥 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사이다',
          price: '3,000원',
          description: '깔끔하게 마시기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '5,000원',
          description: '튀김과 잘 어울리는 생맥주 1잔.',
        },
      ],
    },
  ],
  59: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  60: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  61: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  62: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  63: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  64: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  65: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  66: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  67: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  68: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  69: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  70: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  71: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  72: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '민물장어 한마리',
          price: '52,000원',
          description: '두툼한 민물장어 한마리를 통째로 구워 제공.',
        },
        {
          id: 'main-2',
          name: '장어구이(소금)',
          price: '29,000원',
          description: '참숯에 구워 장어 본연의 맛을 살린 소금구이.',
        },
        {
          id: 'main-3',
          name: '장어구이(양념)',
          price: '29,000원',
          description: '달콤한 양념을 발라 구운 장어구이.',
        },
        {
          id: 'main-4',
          name: '장어덮밥',
          price: '22,000원',
          description: '구운 장어를 밥 위에 올린 든든한 덮밥.',
        },
        {
          id: 'main-5',
          name: '장어탕',
          price: '14,000원',
          description: '구수하게 끓여낸 장어탕.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥',
          price: '1,000원',
          description: '구이와 곁들이는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '4,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '장어구이와 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  73: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  74: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '제철 회를 다양하게 구성한 사시미 모둠.',
        },
        {
          id: 'main-2',
          name: '연어아보카도동',
          price: '18,000원',
          description: '연어와 아보카도를 함께 즐기는 덮밥.',
        },
        {
          id: 'main-3',
          name: '우니동',
          price: '32,000원',
          description: '고소한 성게알(우니)을 올린 덮밥.',
        },
        {
          id: 'main-4',
          name: '카이센동',
          price: '17,000원',
          description: '신선한 해산물을 듬뿍 올린 일본식 해산물덮밥.',
        },
        {
          id: 'main-5',
          name: '혼마구로동',
          price: '24,000원',
          description: '진한 풍미의 참다랑어를 올린 덮밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '덮밥과 곁들이기 좋은 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '사케',
          price: '12,000원',
          description: '해산물과 잘 어울리는 사케 1병(소)',
        },
      ],
    },
  ],
  75: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  76: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  77: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '봉골레파스타',
          price: '19,000원',
          description: '조개와 화이트와인 소스로 만든 파스타.',
        },
        {
          id: 'main-2',
          name: '시저샐러드',
          price: '16,000원',
          description: '로메인과 치즈를 곁들인 클래식 샐러드.',
        },
        {
          id: 'main-3',
          name: '안심스테이크',
          price: '36,000원',
          description: '부드러운 안심 부위를 굽기 정도에 맞춰 제공.',
        },
        {
          id: 'main-4',
          name: '채끝스테이크',
          price: '32,000원',
          description: '풍미가 진한 채끝을 구워낸 스테이크.',
        },
        {
          id: 'main-5',
          name: '트러플크림리조또',
          price: '21,000원',
          description: '트러플 향을 더한 크림 리조또.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '겉바속촉으로 튀긴 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '버섯크림수프',
          price: '9,000원',
          description: '부드럽게 끓여낸 버섯크림수프.',
        },
        {
          id: 'sub-3',
          name: '버팔로윙',
          price: '12,000원',
          description: '매콤한 소스로 버무린 버팔로윙.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '글라스와인',
          price: '12,000원',
          description: '가볍게 즐기는 글라스와인 1잔.',
        },
        {
          id: 'other-2',
          name: '생맥주',
          price: '6,000원',
          description: '요리와 함께 즐기는 생맥주 1잔.',
        },
      ],
    },
  ],
  78: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '복매운탕',
          price: '24,000원',
          description: '얼큰한 국물로 즐기는 복매운탕.',
        },
        {
          id: 'main-2',
          name: '복불고기',
          price: '28,000원',
          description: '복어살을 양념해 구워낸 복불고기.',
        },
        {
          id: 'main-3',
          name: '복사시미',
          price: '45,000원',
          description: '얇게 썰어낸 복사시미(소).',
        },
        {
          id: 'main-4',
          name: '복지리',
          price: '24,000원',
          description: '맑고 시원하게 끓여낸 복지리.',
        },
        {
          id: 'main-5',
          name: '복튀김',
          price: '26,000원',
          description: '복어살을 바삭하게 튀겨낸 튀김.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '가볍게 곁들이는 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '복껍질무침',
          price: '12,000원',
          description: '쫄깃한 복껍질을 새콤하게 무친 메뉴.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '사케',
          price: '12,000원',
          description: '복요리와 어울리는 사케 1병(소)',
        },
      ],
    },
  ],
  79: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈코츠라멘',
          price: '11,000원',
          description: '진하게 우린 돼지뼈 육수에 차슈를 올린 라멘.',
        },
        {
          id: 'main-2',
          name: '미소라멘',
          price: '11,000원',
          description: '된장 풍미가 살아있는 미소 라멘.',
        },
        {
          id: 'main-3',
          name: '쇼유라멘',
          price: '10,500원',
          description: '간장 베이스 국물로 깔끔하게 즐기는 라멘.',
        },
        {
          id: 'main-4',
          name: '차슈덮밥',
          price: '10,000원',
          description: '달짝지근한 차슈와 밥을 함께 즐기는 덮밥.',
        },
        {
          id: 'main-5',
          name: '츠케멘',
          price: '12,000원',
          description: '진한 찍먹 소스에 면을 담가 먹는 라멘.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게 5조각',
          price: '7,000원',
          description: '촉촉하게 튀겨낸 가라아게 5조각.',
        },
        {
          id: 'sub-2',
          name: '교자만두 5개',
          price: '5,000원',
          description: '바삭하게 구운 교자만두 5개.',
        },
        {
          id: 'sub-3',
          name: '아지타마고',
          price: '3,000원',
          description: '반숙으로 간을 배인 라멘 토핑 계란 1개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '탄산음료',
          price: '3,000원',
          description: '가볍게 곁들이기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '라멘과 잘 어울리는 하이볼 1잔.',
        },
      ],
    },
  ],
  80: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  81: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '냉채족발',
          price: '43,000원',
          description: '새콤한 겨자소스와 즐기는 냉채족발.',
        },
        {
          id: 'main-2',
          name: '막국수',
          price: '10,000원',
          description: '족발과 궁합 좋은 시원한 막국수.',
        },
        {
          id: 'main-3',
          name: '불족발',
          price: '42,000원',
          description: '매콤한 양념을 더한 불족발.',
        },
        {
          id: 'main-4',
          name: '족발(대)',
          price: '45,000원',
          description: '인원수 많은 모임에 좋은 족발(대).',
        },
        {
          id: 'main-5',
          name: '족발(중)',
          price: '38,000원',
          description: '윤기 있게 삶아낸 쫄깃한 족발(중).',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '주먹밥',
          price: '4,000원',
          description: '매콤한 메뉴와 함께 먹기 좋은 주먹밥.',
        },
        {
          id: 'sub-3',
          name: '파전',
          price: '12,000원',
          description: '바삭하게 부친 파전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '족발과 어울리는 소주 1병.',
        },
      ],
    },
  ],
  82: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '양갈비',
          price: '28,000원',
          description: '두툼한 양갈비를 숯불에 구워 즐기는 메뉴.',
        },
        {
          id: 'main-2',
          name: '양갈비살',
          price: '25,000원',
          description: '먹기 좋게 손질한 양갈비살 구이.',
        },
        {
          id: 'main-3',
          name: '양고기볶음밥',
          price: '12,000원',
          description: '양고기와 채소를 넣어 볶은 고소한 볶음밥.',
        },
        {
          id: 'main-4',
          name: '양꼬치 10꼬치',
          price: '19,000원',
          description: '향신료를 더해 구운 양꼬치 10꼬치.',
        },
        {
          id: 'main-5',
          name: '양등심',
          price: '26,000원',
          description: '담백하고 부드러운 양등심 구이.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '꿔바로우',
          price: '12,000원',
          description: '바삭한 튀김옷에 새콤한 소스를 입힌 꿔바로우.',
        },
        {
          id: 'sub-2',
          name: '마파두부',
          price: '11,000원',
          description: '얼큰한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'sub-3',
          name: '온면',
          price: '9,000원',
          description: '따뜻한 국물에 면을 말아낸 중국식 온면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '칭따오',
          price: '6,500원',
          description: '양고기와 잘 어울리는 칭따오 맥주 1병.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '깔끔하게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  83: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '눈다랑어 등살',
          price: '48,000원',
          description: '담백한 식감의 눈다랑어 등살.',
        },
        {
          id: 'main-2',
          name: '참다랑어 뱃살',
          price: '65,000원',
          description: '고소한 지방이 풍부한 참다랑어 뱃살.',
        },
        {
          id: 'main-3',
          name: '참치모둠회(소)',
          price: '55,000원',
          description: '부위별로 골고루 즐기는 참치 모둠회(소).',
        },
        {
          id: 'main-4',
          name: '참치초밥 10피스',
          price: '22,000원',
          description: '참치 중심으로 구성한 초밥 10피스.',
        },
        {
          id: 'main-5',
          name: '참치타다키',
          price: '25,000원',
          description: '겉만 살짝 구워 향을 살린 참치 타다키.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게',
          price: '8,000원',
          description: '겉은 바삭, 속은 촉촉한 치킨 가라아게.',
        },
        {
          id: 'sub-2',
          name: '계란말이',
          price: '7,000원',
          description: '폭신하게 말아낸 두툼한 계란말이.',
        },
        {
          id: 'sub-3',
          name: '명란구이',
          price: '9,000원',
          description: '짭짤고소한 명란을 구워낸 안주.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '14,000원',
          description: '참치와 궁합 좋은 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '가볍게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  84: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '김치전',
          price: '12,000원',
          description: '바삭하게 부친 김치전.',
        },
        {
          id: 'main-2',
          name: '라면사리부대찌개',
          price: '12,500원',
          description: '라면사리를 넣어 더 든든한 부대찌개.',
        },
        {
          id: 'main-3',
          name: '부대찌개',
          price: '11,000원',
          description: '햄과 소시지를 듬뿍 넣어 끓인 부대찌개.',
        },
        {
          id: 'main-4',
          name: '치즈부대찌개',
          price: '12,500원',
          description: '치즈를 올려 고소함을 더한 부대찌개.',
        },
        {
          id: 'main-5',
          name: '햄모둠구이',
          price: '18,000원',
          description: '소시지와 햄을 구워낸 모둠구이.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란말이',
          price: '7,000원',
          description: '폭신한 계란말이 한 접시.',
        },
        {
          id: 'sub-2',
          name: '라면사리',
          price: '3,000원',
          description: '부대찌개에 넣어 먹는 라면사리.',
        },
        {
          id: 'sub-3',
          name: '치즈추가',
          price: '3,000원',
          description: '치즈를 추가해 고소함을 더함.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '얼큰한 찌개와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  85: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '램마살라커리',
          price: '17,000원',
          description: '향신료를 진하게 사용한 램 커리.',
        },
        {
          id: 'main-2',
          name: '버터치킨커리',
          price: '15,000원',
          description: '부드러운 치킨과 크리미한 소스의 커리.',
        },
        {
          id: 'main-3',
          name: '치킨비리야니',
          price: '16,000원',
          description: '향신료 밥과 치킨을 함께 볶아낸 비리야니.',
        },
        {
          id: 'main-4',
          name: '탄두리치킨',
          price: '18,000원',
          description: '화덕에 구워 향이 살아있는 탄두리 치킨.',
        },
        {
          id: 'main-5',
          name: '팔락파니르',
          price: '16,000원',
          description: '시금치 소스에 파니르 치즈를 넣은 커리.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '갈릭난',
          price: '3,500원',
          description: '갈릭 버터 향이 나는 난 1장.',
        },
        {
          id: 'sub-2',
          name: '망고라씨',
          price: '5,000원',
          description: '달콤한 망고 라씨 1잔.',
        },
        {
          id: 'sub-3',
          name: '플레인난',
          price: '3,000원',
          description: '커리와 잘 어울리는 기본 난 1장.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '인도맥주',
          price: '7,000원',
          description: '향신료 음식과 잘 어울리는 인도맥주 1병.',
        },
        {
          id: 'other-2',
          name: '탄산음료',
          price: '3,000원',
          description: '깔끔한 탄산음료 1잔.',
        },
      ],
    },
  ],
  86: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '제철 회를 다양하게 구성한 사시미 모둠.',
        },
        {
          id: 'main-2',
          name: '연어아보카도동',
          price: '18,000원',
          description: '연어와 아보카도를 함께 즐기는 덮밥.',
        },
        {
          id: 'main-3',
          name: '우니동',
          price: '32,000원',
          description: '고소한 성게알(우니)을 올린 덮밥.',
        },
        {
          id: 'main-4',
          name: '카이센동',
          price: '17,000원',
          description: '신선한 해산물을 듬뿍 올린 일본식 해산물덮밥.',
        },
        {
          id: 'main-5',
          name: '혼마구로동',
          price: '24,000원',
          description: '진한 풍미의 참다랑어를 올린 덮밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '덮밥과 곁들이기 좋은 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '사케',
          price: '12,000원',
          description: '해산물과 잘 어울리는 사케 1병(소)',
        },
      ],
    },
  ],
  87: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '민물장어 한마리',
          price: '52,000원',
          description: '두툼한 민물장어 한마리를 통째로 구워 제공.',
        },
        {
          id: 'main-2',
          name: '장어구이(소금)',
          price: '29,000원',
          description: '참숯에 구워 장어 본연의 맛을 살린 소금구이.',
        },
        {
          id: 'main-3',
          name: '장어구이(양념)',
          price: '29,000원',
          description: '달콤한 양념을 발라 구운 장어구이.',
        },
        {
          id: 'main-4',
          name: '장어덮밥',
          price: '22,000원',
          description: '구운 장어를 밥 위에 올린 든든한 덮밥.',
        },
        {
          id: 'main-5',
          name: '장어탕',
          price: '14,000원',
          description: '구수하게 끓여낸 장어탕.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '공기밥',
          price: '1,000원',
          description: '구이와 곁들이는 공기밥 1그릇.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '4,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '장어구이와 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  88: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '16,000원',
          description: '진하게 우려낸 갈비탕에 당면을 곁들인 보양 국물.',
        },
        {
          id: 'main-2',
          name: '물냉면',
          price: '12,000원',
          description: '동치미와 육수를 섞어 시원하게 즐기는 냉면.',
        },
        {
          id: 'main-3',
          name: '비빔냉면',
          price: '12,000원',
          description: '매콤달콤한 양념에 비벼 먹는 냉면.',
        },
        {
          id: 'main-4',
          name: '수육',
          price: '30,000원',
          description: '부드럽게 삶아낸 돼지고기 수육을 넉넉히 제공.',
        },
        {
          id: 'main-5',
          name: '회냉면',
          price: '14,000원',
          description: '새콤한 양념과 회를 올려 입맛 돋우는 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '녹두전',
          price: '9,000원',
          description: '고소하게 부친 녹두전 한 장.',
        },
        {
          id: 'sub-2',
          name: '비빔만두',
          price: '8,000원',
          description: '만두를 매콤한 양념에 버무린 인기 메뉴.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '냉면과 같이 먹기 좋은 왕만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기기 좋은 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '사이다',
          price: '3,000원',
          description: '매콤한 메뉴와 어울리는 탄산음료.',
        },
      ],
    },
  ],
  89: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  90: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '누룽지백숙',
          price: '65,000원',
          description: '누룽지로 고소함을 더한 백숙.',
        },
        {
          id: 'main-2',
          name: '능이백숙',
          price: '65,000원',
          description: '능이버섯을 넣어 푹 고아낸 백숙.',
        },
        {
          id: 'main-3',
          name: '닭볶음탕',
          price: '38,000원',
          description: '매콤하게 졸여낸 닭볶음탕.',
        },
        {
          id: 'main-4',
          name: '닭죽',
          price: '12,000원',
          description: '백숙 국물로 끓여낸 부드러운 닭죽.',
        },
        {
          id: 'main-5',
          name: '한방백숙',
          price: '60,000원',
          description: '한방 재료로 끓여 몸을 따뜻하게 하는 백숙.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자전',
          price: '9,000원',
          description: '고소하게 부친 감자전.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤하게 무친 도토리묵.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '바삭하게 부친 해물파전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '따뜻한 국물과 어울리는 소주 1병.',
        },
      ],
    },
  ],
  91: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '닭내장닭갈비',
          price: '16,000원',
          description: '쫄깃한 내장을 함께 볶아낸 닭갈비.',
        },
        {
          id: 'main-2',
          name: '막국수',
          price: '10,000원',
          description: '닭갈비와 잘 어울리는 시원한 막국수.',
        },
        {
          id: 'main-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥(1인).',
        },
        {
          id: 'main-4',
          name: '춘천닭갈비',
          price: '14,000원',
          description: '매콤한 양념에 닭고기와 채소를 볶아낸 닭갈비.',
        },
        {
          id: 'main-5',
          name: '치즈닭갈비',
          price: '16,000원',
          description: '치즈를 듬뿍 올려 고소함을 더한 닭갈비.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '떡사리',
          price: '3,000원',
          description: '쫄깃한 떡을 추가해 즐기는 사리.',
        },
        {
          id: 'sub-2',
          name: '우동사리',
          price: '3,000원',
          description: '닭갈비에 넣어 볶아 먹는 우동사리.',
        },
        {
          id: 'sub-3',
          name: '치즈추가',
          price: '3,000원',
          description: '치즈를 더해 풍미를 살림.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '매콤한 닭갈비와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  92: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  93: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  94: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  95: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  96: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  97: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '공기밥',
          price: '1,000원',
          description: '추어탕과 함께 먹는 공기밥.',
        },
        {
          id: 'main-2',
          name: '우렁추어탕',
          price: '14,000원',
          description: '우렁이를 더해 식감을 살린 추어탕.',
        },
        {
          id: 'main-3',
          name: '추어탕',
          price: '12,000원',
          description: '미꾸라지를 갈아 넣어 구수하게 끓인 추어탕.',
        },
        {
          id: 'main-4',
          name: '추어튀김',
          price: '18,000원',
          description: '바삭하게 튀겨낸 추어튀김.',
        },
        {
          id: 'main-5',
          name: '통추어탕',
          price: '13,000원',
          description: '통미꾸라지를 넣어 진하게 끓인 추어탕.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자전',
          price: '9,000원',
          description: '고소하게 부친 감자전.',
        },
        {
          id: 'sub-2',
          name: '도토리묵무침',
          price: '8,000원',
          description: '새콤한 도토리묵무침.',
        },
        {
          id: 'sub-3',
          name: '부추전',
          price: '9,000원',
          description: '부추를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  98: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '전복해물탕',
          price: '58,000원',
          description: '전복을 더해 깊은 맛을 낸 해물탕.',
        },
        {
          id: 'main-2',
          name: '해물모둠',
          price: '45,000원',
          description: '제철 해산물을 다양하게 즐기는 해물모둠.',
        },
        {
          id: 'main-3',
          name: '해물찜',
          price: '52,000원',
          description: '매콤한 양념에 해산물을 쪄낸 해물찜.',
        },
        {
          id: 'main-4',
          name: '해물탕',
          price: '48,000원',
          description: '전복, 새우, 조개를 넣어 시원하게 끓인 해물탕.',
        },
        {
          id: 'main-5',
          name: '회 모둠(소)',
          price: '38,000원',
          description: '신선한 회를 모둠으로 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살볶음밥',
          price: '9,000원',
          description: '고소한 게살을 넣어 볶은 볶음밥.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭한 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '해물라면',
          price: '8,000원',
          description: '해산물 육수로 끓인 얼큰한 해물라면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '해산물과 잘 어울리는 소주 1병.',
        },
      ],
    },
  ],
  99: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  100: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  101: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '시래기코다리조림',
          price: '30,000원',
          description: '시래기를 넣어 깊은 맛을 더한 코다리조림.',
        },
        {
          id: 'main-2',
          name: '코다리구이',
          price: '24,000원',
          description: '노릇하게 구워낸 코다리구이.',
        },
        {
          id: 'main-3',
          name: '코다리냉면',
          price: '12,000원',
          description: '코다리무침을 올려 새콤하게 즐기는 냉면.',
        },
        {
          id: 'main-4',
          name: '코다리조림',
          price: '28,000원',
          description: '매콤달콤한 양념에 코다리를 졸여낸 조림.',
        },
        {
          id: 'main-5',
          name: '코다리찜',
          price: '32,000원',
          description: '부드럽게 쪄낸 코다리에 양념을 더한 찜.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자전',
          price: '9,000원',
          description: '바삭하게 부친 감자전.',
        },
        {
          id: 'sub-2',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-3',
          name: '메밀전병 2개',
          price: '8,000원',
          description: '고소하게 구운 메밀전병 2개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '한식과 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  102: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈코츠라멘',
          price: '11,000원',
          description: '진하게 우린 돼지뼈 육수 라멘.',
        },
        {
          id: 'main-2',
          name: '쇼유라멘',
          price: '10,500원',
          description: '간장 베이스 라멘.',
        },
        {
          id: 'main-3',
          name: '지로라멘',
          price: '13,000원',
          description: '진한 육수에 두꺼운 면과 토핑을 푸짐하게 올린 라멘.',
        },
        {
          id: 'main-4',
          name: '차슈덮밥',
          price: '10,000원',
          description: '차슈를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '츠케멘',
          price: '12,000원',
          description: '찍먹 스타일 라멘.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '가라아게 5조각',
          price: '7,000원',
          description: '촉촉하게 튀겨낸 가라아게 5조각.',
        },
        {
          id: 'sub-2',
          name: '교자만두 5개',
          price: '5,000원',
          description: '바삭하게 구운 교자만두 5개.',
        },
        {
          id: 'sub-3',
          name: '아지타마고',
          price: '3,000원',
          description: '반숙으로 간을 배인 라멘 토핑 계란 1개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '탄산음료',
          price: '3,000원',
          description: '가볍게 곁들이기 좋은 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '라멘과 잘 어울리는 하이볼 1잔.',
        },
      ],
    },
  ],
  103: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '게딱지볶음밥',
          price: '12,000원',
          description: '게내장과 밥을 볶아 고소한 맛이 나는 볶음밥.',
        },
        {
          id: 'main-2',
          name: '대게찜',
          price: '95,000원',
          description: '살이 꽉 찬 대게를 쪄낸 메인 메뉴.',
        },
        {
          id: 'main-3',
          name: '랍스터구이',
          price: '85,000원',
          description: '버터향을 더해 구워낸 랍스터구이.',
        },
        {
          id: 'main-4',
          name: '킹크랩찜',
          price: '160,000원',
          description: '킹크랩을 쪄내 풍부한 살맛을 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '해물모둠',
          price: '68,000원',
          description: '대게와 조개, 새우를 함께 즐기는 모둠.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살전',
          price: '15,000원',
          description: '게살을 듬뿍 넣어 부친 전.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '해물라면',
          price: '9,000원',
          description: '게 육수에 끓여낸 해물라면.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '6,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '해산물과 어울리는 소주 1병.',
        },
      ],
    },
  ],
  104: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '램마살라커리',
          price: '17,000원',
          description: '향신료를 진하게 사용한 램 커리.',
        },
        {
          id: 'main-2',
          name: '버터치킨커리',
          price: '15,000원',
          description: '부드러운 치킨과 크리미한 소스의 커리.',
        },
        {
          id: 'main-3',
          name: '치킨비리야니',
          price: '16,000원',
          description: '향신료 밥과 치킨을 함께 볶아낸 비리야니.',
        },
        {
          id: 'main-4',
          name: '탄두리치킨',
          price: '18,000원',
          description: '화덕에 구워 향이 살아있는 탄두리 치킨.',
        },
        {
          id: 'main-5',
          name: '팔락파니르',
          price: '16,000원',
          description: '시금치 소스에 파니르 치즈를 넣은 커리.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '갈릭난',
          price: '3,500원',
          description: '갈릭 버터 향이 나는 난 1장.',
        },
        {
          id: 'sub-2',
          name: '망고라씨',
          price: '5,000원',
          description: '달콤한 망고 라씨 1잔.',
        },
        {
          id: 'sub-3',
          name: '플레인난',
          price: '3,000원',
          description: '커리와 잘 어울리는 기본 난 1장.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '인도맥주',
          price: '7,000원',
          description: '향신료 음식과 잘 어울리는 인도맥주 1병.',
        },
        {
          id: 'other-2',
          name: '탄산음료',
          price: '3,000원',
          description: '깔끔한 탄산음료 1잔.',
        },
      ],
    },
  ],
  105: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '돈가스김치나베',
          price: '16,000원',
          description: '얼큰한 김치나베에 돈가스를 곁들인 따뜻한 메뉴.',
        },
        {
          id: 'main-2',
          name: '로스카츠',
          price: '12,000원',
          description: '등심을 두툼하게 튀겨 바삭함과 육즙을 살린 돈가스.',
        },
        {
          id: 'main-3',
          name: '치즈카츠',
          price: '15,000원',
          description: '치즈를 넣어 고소함을 더한 돈가스.',
        },
        {
          id: 'main-4',
          name: '카레카츠',
          price: '15,000원',
          description: '진한 카레에 돈가스를 올려 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '히레카츠',
          price: '14,000원',
          description: '안심을 부드럽게 튀겨 촉촉한 식감이 특징.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '돈가스와 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '치킨가라아게',
          price: '7,000원',
          description: '겉은 바삭, 속은 촉촉한 가라아게 한 접시.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '콜라',
          price: '3,000원',
          description: '깔끔하게 즐기는 탄산음료.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '돈가스와 잘 어울리는 시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  106: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  107: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '16,000원',
          description: '진하게 우려낸 갈비탕에 당면을 곁들인 보양 국물.',
        },
        {
          id: 'main-2',
          name: '물냉면',
          price: '12,000원',
          description: '동치미와 육수를 섞어 시원하게 즐기는 냉면.',
        },
        {
          id: 'main-3',
          name: '비빔냉면',
          price: '12,000원',
          description: '매콤달콤한 양념에 비벼 먹는 냉면.',
        },
        {
          id: 'main-4',
          name: '수육',
          price: '30,000원',
          description: '부드럽게 삶아낸 돼지고기 수육을 넉넉히 제공.',
        },
        {
          id: 'main-5',
          name: '회냉면',
          price: '14,000원',
          description: '새콤한 양념과 회를 올려 입맛 돋우는 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '녹두전',
          price: '9,000원',
          description: '고소하게 부친 녹두전 한 장.',
        },
        {
          id: 'sub-2',
          name: '비빔만두',
          price: '8,000원',
          description: '만두를 매콤한 양념에 버무린 인기 메뉴.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '냉면과 같이 먹기 좋은 왕만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기기 좋은 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '사이다',
          price: '3,000원',
          description: '매콤한 메뉴와 어울리는 탄산음료.',
        },
      ],
    },
  ],
  108: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '16,000원',
          description: '진하게 우려낸 갈비탕에 당면을 곁들인 보양 국물.',
        },
        {
          id: 'main-2',
          name: '물냉면',
          price: '12,000원',
          description: '동치미와 육수를 섞어 시원하게 즐기는 냉면.',
        },
        {
          id: 'main-3',
          name: '비빔냉면',
          price: '12,000원',
          description: '매콤달콤한 양념에 비벼 먹는 냉면.',
        },
        {
          id: 'main-4',
          name: '수육',
          price: '30,000원',
          description: '부드럽게 삶아낸 돼지고기 수육을 넉넉히 제공.',
        },
        {
          id: 'main-5',
          name: '회냉면',
          price: '14,000원',
          description: '새콤한 양념과 회를 올려 입맛 돋우는 냉면.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '녹두전',
          price: '9,000원',
          description: '고소하게 부친 녹두전 한 장.',
        },
        {
          id: 'sub-2',
          name: '비빔만두',
          price: '8,000원',
          description: '만두를 매콤한 양념에 버무린 인기 메뉴.',
        },
        {
          id: 'sub-3',
          name: '왕만두 4개',
          price: '6,000원',
          description: '냉면과 같이 먹기 좋은 왕만두 4개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 함께 즐기기 좋은 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '사이다',
          price: '3,000원',
          description: '매콤한 메뉴와 어울리는 탄산음료.',
        },
      ],
    },
  ],
  109: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-2',
          name: '이베리코 갈비살',
          price: '26,000원',
          description: '진한 육향의 갈비살을 구워내는 메뉴.',
        },
        {
          id: 'main-3',
          name: '이베리코 꽃목살',
          price: '23,000원',
          description: '마블링이 좋은 꽃목살 부위를 구워 제공.',
        },
        {
          id: 'main-4',
          name: '이베리코 목살',
          price: '21,000원',
          description: '스페인산 이베리코 목살을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '이베리코 항정살',
          price: '24,000원',
          description: '고소한 지방이 매력적인 항정살 구이.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '이베리코와 잘 어울리는 하이볼 1잔.',
        },
      ],
    },
  ],
  110: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  111: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '덴푸라소바',
          price: '15,000원',
          description: '새우 덴푸라를 올린 소바.',
        },
        {
          id: 'main-2',
          name: '모둠덴푸라',
          price: '18,000원',
          description: '새우와 제철 야채를 튀겨낸 덴푸라 모둠.',
        },
        {
          id: 'main-3',
          name: '온소바',
          price: '11,000원',
          description: '따뜻한 육수에 담아낸 온소바.',
        },
        {
          id: 'main-4',
          name: '자루소바',
          price: '11,000원',
          description: '직접 제분한 메밀로 만든 시원한 자루소바.',
        },
        {
          id: 'main-5',
          name: '토리텐붓카케',
          price: '14,000원',
          description: '치킨텐을 올린 차가운 붓카케 스타일.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '새우덴푸라 2마리',
          price: '7,000원',
          description: '바삭하게 튀긴 새우 덴푸라 2마리.',
        },
        {
          id: 'sub-2',
          name: '유부초밥 4개',
          price: '4,000원',
          description: '소바와 함께 즐기기 좋은 유부초밥 4개.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '생맥주',
          price: '5,000원',
          description: '덴푸라와 궁합 좋은 생맥주 1잔.',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원하게 즐기는 하이볼 1잔.',
        },
      ],
    },
  ],
  112: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  113: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '마파두부',
          price: '18,000원',
          description: '매콤한 소스에 두부를 볶아낸 마파두부.',
        },
        {
          id: 'main-2',
          name: '유린기',
          price: '22,000원',
          description: '바삭한 닭튀김에 상큼한 소스를 올린 유린기.',
        },
        {
          id: 'main-3',
          name: '짜장면',
          price: '9,000원',
          description: '춘장을 볶아 고소하게 만든 기본 짜장면.',
        },
        {
          id: 'main-4',
          name: '짬뽕',
          price: '11,000원',
          description: '해물과 채소를 넣어 얼큰하게 끓인 짬뽕.',
        },
        {
          id: 'main-5',
          name: '탕수육',
          price: '20,000원',
          description: '바삭한 돼지고기 튀김에 새콤달콤 소스를 곁들인 탕수육.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '게살스프',
          price: '8,000원',
          description: '부드럽게 끓여낸 게살스프.',
        },
        {
          id: 'sub-2',
          name: '군만두 6개',
          price: '6,000원',
          description: '바삭하게 구운 군만두 6개.',
        },
        {
          id: 'sub-3',
          name: '춘권 3개',
          price: '6,000원',
          description: '속을 채워 튀긴 춘권 3개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
        {
          id: 'other-2',
          name: '칭따오',
          price: '6,500원',
          description: '중식과 잘 어울리는 칭따오 맥주 1병.',
        },
      ],
    },
  ],
  114: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  115: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  116: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '꼬막비빔밥',
          price: '15,000원',
          description: '꼬막을 듬뿍 올린 비빔밥.',
        },
        {
          id: 'main-2',
          name: '낙지볶음',
          price: '18,000원',
          description: '매콤하게 볶아낸 낙지볶음.',
        },
        {
          id: 'main-3',
          name: '전복비빔밥',
          price: '16,000원',
          description: '전복을 올려 고소함을 더한 비빔밥.',
        },
        {
          id: 'main-4',
          name: '해물된장찌개',
          price: '11,000원',
          description: '해물을 넣어 시원한 맛을 더한 된장찌개.',
        },
        {
          id: 'main-5',
          name: '해초비빔밥',
          price: '12,000원',
          description: '해초와 나물을 넣어 상큼하게 비벼 먹는 비빔밥.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '김부각',
          price: '6,000원',
          description: '바삭하게 튀겨낸 김부각 안주.',
        },
        {
          id: 'sub-2',
          name: '꼬막무침',
          price: '14,000원',
          description: '새콤하게 무친 꼬막무침.',
        },
        {
          id: 'sub-3',
          name: '해물파전',
          price: '14,000원',
          description: '해물과 파를 듬뿍 넣어 부친 전.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '막걸리',
          price: '6,000원',
          description: '전과 잘 어울리는 막걸리 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  117: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '간장치킨',
          price: '20,000원',
          description: '짭짤한 간장소스로 맛을 낸 치킨.',
        },
        {
          id: 'main-2',
          name: '순살치킨',
          price: '21,000원',
          description: '먹기 좋은 순살로 즐기는 치킨.',
        },
        {
          id: 'main-3',
          name: '양념치킨',
          price: '20,000원',
          description: '달콤매콤한 양념을 버무린 양념치킨.',
        },
        {
          id: 'main-4',
          name: '치킨강정',
          price: '19,000원',
          description: '바삭한 치킨에 소스를 입힌 치킨강정.',
        },
        {
          id: 'main-5',
          name: '후라이드치킨',
          price: '19,000원',
          description: '겉은 바삭, 속은 촉촉한 후라이드치킨.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '감자튀김',
          price: '7,000원',
          description: '치킨과 잘 어울리는 감자튀김.',
        },
        {
          id: 'sub-2',
          name: '오징어튀김',
          price: '9,000원',
          description: '바삭하게 튀긴 오징어튀김.',
        },
        {
          id: 'sub-3',
          name: '치즈볼 5개',
          price: '6,000원',
          description: '고소한 치즈가 들어간 치즈볼 5개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '생맥주',
          price: '6,000원',
          description: '치킨과 찰떡궁합인 생맥주 1잔.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '깔끔한 소주 1병.',
        },
      ],
    },
  ],
  118: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '복매운탕',
          price: '24,000원',
          description: '얼큰한 국물로 즐기는 복매운탕.',
        },
        {
          id: 'main-2',
          name: '복불고기',
          price: '28,000원',
          description: '복어살을 양념해 구워낸 복불고기.',
        },
        {
          id: 'main-3',
          name: '복사시미',
          price: '45,000원',
          description: '얇게 썰어낸 복사시미(소).',
        },
        {
          id: 'main-4',
          name: '복지리',
          price: '24,000원',
          description: '맑고 시원하게 끓여낸 복지리.',
        },
        {
          id: 'main-5',
          name: '복튀김',
          price: '26,000원',
          description: '복어살을 바삭하게 튀겨낸 튀김.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '가볍게 곁들이는 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '복껍질무침',
          price: '12,000원',
          description: '쫄깃한 복껍질을 새콤하게 무친 메뉴.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '사케',
          price: '12,000원',
          description: '복요리와 어울리는 사케 1병(소)',
        },
      ],
    },
  ],
  119: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '모둠초밥 12피스',
          price: '18,000원',
          description: '제철 생선으로 구성한 모둠초밥 12피스.',
        },
        {
          id: 'main-2',
          name: '사시미 모둠(소)',
          price: '38,000원',
          description: '숙성된 회를 다양하게 즐기는 사시미 모둠.',
        },
        {
          id: 'main-3',
          name: '연어덮밥',
          price: '16,000원',
          description: '두툼한 연어와 간장소스로 즐기는 덮밥.',
        },
        {
          id: 'main-4',
          name: '참치덮밥',
          price: '18,000원',
          description: '부드러운 참치와 김가루를 올린 덮밥.',
        },
        {
          id: 'main-5',
          name: '특선초밥 12피스',
          price: '24,000원',
          description: '프리미엄 재료를 더한 특선 초밥 12피스.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '미니우동',
          price: '4,000원',
          description: '초밥과 잘 어울리는 따뜻한 미니 우동.',
        },
        {
          id: 'sub-2',
          name: '새우튀김 2마리',
          price: '6,000원',
          description: '바삭하게 튀긴 새우튀김 2마리.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '초밥과 잘 어울리는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
  120: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가브리살',
          price: '19,000원',
          description: '부드럽고 진한 맛의 가브리살.',
        },
        {
          id: 'main-2',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 구수한 된장찌개.',
        },
        {
          id: 'main-3',
          name: '숙성목살',
          price: '17,000원',
          description: '육즙을 살린 숙성 목살 구이.',
        },
        {
          id: 'main-4',
          name: '숙성삼겹살',
          price: '17,000원',
          description: '숙성한 삼겹살을 두툼하게 썰어 구워내는 메뉴.',
        },
        {
          id: 'main-5',
          name: '항정살',
          price: '19,000원',
          description: '고소한 지방과 쫄깃한 식감의 항정살.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '고기와 함께 즐기기 좋은 부드러운 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 곁들이기 좋은 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '볶음밥',
          price: '3,000원',
          description: '마무리로 즐기는 고소한 볶음밥.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원하게 즐기는 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '고기와 찰떡궁합인 소주 1병.',
        },
      ],
    },
  ],
  121: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '갈비탕',
          price: '17,000원',
          description: '진하게 우려낸 갈비탕으로 든든하게.',
        },
        {
          id: 'main-2',
          name: '육회',
          price: '25,000원',
          description: '신선한 한우를 양념해 고소하게 즐기는 육회.',
        },
        {
          id: 'main-3',
          name: '한우갈비살',
          price: '42,000원',
          description: '쫄깃한 식감과 진한 육향의 한우 갈비살.',
        },
        {
          id: 'main-4',
          name: '한우등심',
          price: '38,000원',
          description: '마블링이 좋은 한우 등심을 구워 즐기는 메뉴.',
        },
        {
          id: 'main-5',
          name: '한우채끝',
          price: '45,000원',
          description: '풍미가 진한 채끝 부위를 구워 즐기는 메뉴.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '계란찜',
          price: '4,000원',
          description: '부드럽게 익힌 계란찜.',
        },
        {
          id: 'sub-2',
          name: '냉면',
          price: '9,000원',
          description: '고기와 잘 어울리는 시원한 냉면.',
        },
        {
          id: 'sub-3',
          name: '된장찌개',
          price: '6,000원',
          description: '구이와 함께 즐기기 좋은 된장찌개.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '맥주',
          price: '5,000원',
          description: '시원한 병맥주 1병.',
        },
        {
          id: 'other-2',
          name: '소주',
          price: '5,000원',
          description: '한우와 어울리는 소주 1병.',
        },
      ],
    },
  ],
  122: [
    {
      id: 'main',
      name: '메인 메뉴',
      items: [
        {
          id: 'main-1',
          name: '가라아게',
          price: '18,000원',
          description: '겉은 바삭, 속은 촉촉한 치킨 가라아게.',
        },
        {
          id: 'main-2',
          name: '나가사키짬뽕',
          price: '15,000원',
          description: '해산물과 야채로 시원하게 끓인 일본식 짬뽕.',
        },
        {
          id: 'main-3',
          name: '명란크림우동',
          price: '16,000원',
          description: '명란과 크림소스로 고소하게 만든 우동.',
        },
        {
          id: 'main-4',
          name: '모둠사시미',
          price: '45,000원',
          description: '제철 생선으로 구성한 사시미 모둠.',
        },
        {
          id: 'main-5',
          name: '연어사시미',
          price: '28,000원',
          description: '두툼하게 썬 연어 사시미.',
        },
      ],
    },
    {
      id: 'sub',
      name: '사이드 메뉴',
      items: [
        {
          id: 'sub-1',
          name: '모찌리도후',
          price: '7,000원',
          description: '쫀득한 식감의 모찌리도후.',
        },
        {
          id: 'sub-2',
          name: '에다마메',
          price: '5,000원',
          description: '소금을 뿌려 삶아낸 에다마메.',
        },
        {
          id: 'sub-3',
          name: '차완무시',
          price: '4,000원',
          description: '부드러운 일본식 계란찜.',
        },
      ],
    },
    {
      id: 'other',
      name: '음료/기타',
      items: [
        {
          id: 'other-1',
          name: '사케',
          price: '12,000원',
          description: '가볍게 즐기는 사케 1병(소)',
        },
        {
          id: 'other-2',
          name: '하이볼',
          price: '9,000원',
          description: '시원한 하이볼 1잔.',
        },
      ],
    },
  ],
};

const cloneMenuCategories = (categories) =>
  categories.map((category) => ({
    ...category,
    items: category.items.map((item) => ({ ...item })),
  }));

export const getMenuCategoriesByRestaurant = (restaurantId) => {
  const categories =
    restaurantMenuCatalog[restaurantId] ?? defaultMenuCategories;
  return cloneMenuCategories(categories);
};

export { defaultMenuCategories, restaurantMenuCatalog };
